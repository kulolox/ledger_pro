package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionListResponse;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.ITransactionService;
import com.example.back_end.service.IUserCategoryService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.SanitizeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements ITransactionService {

    private static final String[] CSV_HEADERS = {
        "type", "amount", "category", "account_id", "to_account_id", "remark", "trans_time"
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IUserCategoryService userCategoryService;
    private final AccountBookMapper accountBookMapper;
    private final CacheManager cacheManager;

    public TransactionServiceImpl(IUserCategoryService userCategoryService, AccountBookMapper accountBookMapper, CacheManager cacheManager) {
        this.userCategoryService = userCategoryService;
        this.accountBookMapper = accountBookMapper;
        this.cacheManager = cacheManager;
    }

    private void validateBookOwnership(Long bookId) {
        var book = accountBookMapper.selectById(bookId);
        if (book == null || !book.getOwnerId().equals(AuthUtils.getCurrentUserId())) {
            throw new BizException("无权访问该账本");
        }
    }

    private List<Long> getOwnedBookIds() {
        return accountBookMapper.selectList(
                        new LambdaQueryWrapper<AccountBook>()
                                .eq(AccountBook::getOwnerId, AuthUtils.getCurrentUserId()))
                .stream()
                .map(AccountBook::getId)
                .toList();
    }

    private String formatCacheKey(Long userId, Long bookId, String month) {
        return userId + "::" + bookId + "::" + month;
    }

    private void evictTransactionCache(Long bookId, String... months) {
        Cache cache = cacheManager.getCache("transactions");
        if (cache != null) {
            Long userId = AuthUtils.getCurrentUserId();
            for (String month : months) {
                cache.evict(formatCacheKey(userId, bookId, month));
            }
        }
    }

    private String extractMonth(String transTime) {
        if (transTime != null && transTime.length() >= 7) {
            return transTime.substring(0, 7);
        }
        return java.time.YearMonth.now().toString();
    }

    @Transactional
    public TransactionResponse create(TransactionRequest req) {
        validateBookOwnership(req.book_id());
        Transaction tx = toEntity(req);
        save(tx);
        evictTransactionCache(req.book_id(), extractMonth(req.trans_time()));
        return TransactionResponse.from(tx);
    }

    @Transactional
    public Map<String, Object> createBatch(BatchCreateRequest body) {
        if (body.items() == null || body.items().isEmpty()) {
            throw new BizException("items 不能为空");
        }
        var bookIds = new java.util.HashSet<Long>();
        for (var item : body.items()) {
            validateBookOwnership(item.book_id());
            bookIds.add(item.book_id());
        }
        List<Transaction> txs = new ArrayList<>();
        for (TransactionRequest params : body.items()) {
            txs.add(toEntity(params));
        }
        saveBatch(txs);
        bookIds.forEach(bid -> {
            List<String> months = txs.stream()
                    .filter(tx -> tx.getBookId().equals(bid))
                    .map(tx -> extractMonth(tx.getTransTime().toString()))
                    .distinct()
                    .toList();
            evictTransactionCache(bid, months.toArray(new String[0]));
        });
        List<TransactionResponse> list =
                txs.stream().map(TransactionResponse::from).toList();
        return Map.of("list", list, "count", list.size());
    }

    @Transactional
    public int batchDelete(BatchDeleteRequest body) {
        if (body.ids() == null || body.ids().isEmpty()) {
            throw new BizException("ids 不能为空");
        }
        List<Long> ownedBookIds = getOwnedBookIds();
        if (ownedBookIds.isEmpty()) {
            throw new BizException("无权操作");
        }
        List<Transaction> txs = lambdaQuery().in(Transaction::getId, body.ids()).list();
        var affectedBookIds = new java.util.HashSet<Long>();
        for (Transaction tx : txs) {
            if (ownedBookIds.contains(tx.getBookId())) {
                affectedBookIds.add(tx.getBookId());
            }
        }
        UpdateWrapper<Transaction> wrapper = new UpdateWrapper<>();
        wrapper.in("id", body.ids()).in("book_id", ownedBookIds).set("deleted", 1).set("updated_at", LocalDateTime.now());
        int actualDeleted = baseMapper.update(null, wrapper);
        var evictMonths = txs.stream()
                .filter(tx -> ownedBookIds.contains(tx.getBookId()))
                .map(tx -> extractMonth(tx.getTransTime().toString()))
                .distinct()
                .toList();
        affectedBookIds.forEach(bid -> evictTransactionCache(bid, evictMonths.toArray(new String[0])));
        return actualDeleted;
    }

    @Cacheable(value = "transactions", key = "T(com.example.back_end.utils.AuthUtils).getCurrentUserId() + '::' + #bookId + '::' + #month", unless = "#result == null")
    public TransactionListResponse list(Long bookId, String month) {
        validateBookOwnership(bookId);
        return queryTransactions(bookId, month);
    }

    public void exportTransactions(Long bookId, String month, String year, String format, HttpServletResponse response) {
        validateBookOwnership(bookId);
        Map<Long, String> categoryMap = categoryNameMap();

        List<TransactionResponse> txs;
        String safePeriod;
        if (year != null && !year.isBlank()) {
            txs = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                String ym = year + "-" + String.format("%02d", m);
                txs.addAll(queryTransactions(bookId, ym).list());
            }
            safePeriod = year.replaceAll("[\\\\/:*?\"<>|]", "_");
        } else if (month != null && !month.isBlank()) {
            txs = queryTransactions(bookId, month).list();
            safePeriod = month.replaceAll("[\\\\/:*?\"<>|]", "_");
        } else {
            throw new BizException("请提供 month 或 year 参数");
        }

        String safeFormat = format.replaceAll("[\\\\/:*?\"<>|]", "_");
        String filename = "transactions_" + safePeriod + "." + safeFormat;
        response.setCharacterEncoding("UTF-8");

        try {
            switch (format) {
                case "csv" -> exportCsv(txs, categoryMap, response, filename);
                case "excel" -> exportExcel(txs, categoryMap, response, filename);
                case "json" -> exportJson(txs, categoryMap, response, filename);
                default -> throw new BizException("不支持的导出格式: " + format);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    @Transactional
    public Map<String, Object> importTransactions(Long bookId, MultipartFile file, String format) {
        validateBookOwnership(bookId);
        try {
            Map<Long, String> categoryMap = categoryNameMap();
            Map<String, Long> nameToId = categoryMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a));
            Map<Integer, Long> otherCategoryByType = userCategoryService.listAll().stream()
                    .filter(c -> "其他".equals(c.name()))
                    .collect(Collectors.toMap(c -> c.type(), c -> c.id(), (a, b) -> a));

            List<TransactionRequest> items =
                    switch (format) {
                        case "csv" -> parseCsv(file, nameToId, otherCategoryByType);
                        case "excel" -> parseExcel(file, nameToId, otherCategoryByType);
                        case "json" -> parseJson(file, nameToId, otherCategoryByType);
                        default -> throw new BizException("不支持的导入格式: " + format);
                    };

            if (items.isEmpty()) {
                throw new BizException("文件中没有有效数据");
            }

            List<Transaction> txs = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                TransactionRequest req = items.get(i);
                try {
                    Transaction tx = toEntity(req);
                    tx.setBookId(bookId);
                    txs.add(tx);
                } catch (Exception e) {
                    errors.add("第" + (i + 2) + "行: " + e.getMessage());
                }
            }

            if (txs.isEmpty() && !errors.isEmpty()) {
                throw new BizException("所有数据解析失败: " + String.join("; ", errors));
            }

            saveBatch(txs);
            var importMonths = txs.stream()
                    .map(tx -> extractMonth(tx.getTransTime().toString()))
                    .distinct()
                    .toList();
            evictTransactionCache(bookId, importMonths.toArray(new String[0]));
            return Map.of("imported", txs.size(), "errors", errors, "total", items.size());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("导入失败", e);
        }
    }

    private TransactionListResponse queryTransactions(Long bookId, String month) {
        LocalDateTime monthStart = LocalDateTime.parse(month + "-01T00:00:00");
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .ge(Transaction::getTransTime, monthStart)
                .lt(Transaction::getTransTime, monthEnd)
                .orderByDesc(Transaction::getTransTime);

        List<Transaction> txs = list(wrapper);
        List<TransactionResponse> list =
                txs.stream().map(TransactionResponse::from).toList();
        return new TransactionListResponse(list, list.size());
    }

    private Map<Long, String> categoryNameMap() {
        return userCategoryService.listAll().stream().collect(Collectors.toMap(c -> c.id(), c -> c.name(), (a, b) -> a));
    }

    private String formatType(Integer type) {
        return type != null && type == 1 ? "收入" : "支出";
    }

    private String formatAmount(Integer cents) {
        if (cents == null) return "0.00";
        return String.format("%.2f", cents / 100.0);
    }

    private String formatTransTime(String iso) {
        if (iso == null || iso.length() < 19) return iso != null ? iso : "";
        return iso.substring(0, 10) + " " + iso.substring(11, 19);
    }

    private Integer parseType(String s) {
        if (s == null || s.isBlank()) return 0;
        String trimmed = s.trim();
        if (trimmed.equals("收入") || trimmed.equalsIgnoreCase("income")) return 1;
        if (trimmed.equals("支出") || trimmed.equalsIgnoreCase("expense")) return 0;
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer parseAmount(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            double yuan = Double.parseDouble(s.trim());
            if (yuan < 0) return 0;
            double cents = yuan * 100;
            if (cents > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return (int) Math.round(cents);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Long parseCategory(String s, Integer type, Map<String, Long> nameToId, Map<Integer, Long> otherCategoryByType) {
        if (s == null || s.isBlank()) return otherCategoryByType.get(type);
        String trimmed = s.trim();
        Long id = nameToId.get(trimmed);
        if (id != null) return id;
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            return otherCategoryByType.get(type);
        }
    }

    private void exportCsv(
            List<TransactionResponse> txs, Map<Long, String> categoryMap, HttpServletResponse response, String filename)
            throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
        writer.writeNext(CSV_HEADERS);
        for (TransactionResponse tx : txs) {
            writer.writeNext(new String[] {
                formatType(tx.type()),
                formatAmount(tx.amount()),
                tx.category_id() != null ? categoryMap.getOrDefault(tx.category_id(), "") : "",
                tx.account_id() != null ? String.valueOf(tx.account_id()) : "",
                tx.to_account_id() != null ? String.valueOf(tx.to_account_id()) : "",
                SanitizeUtils.sanitizeCsvCell(tx.remark() != null ? tx.remark() : ""),
                formatTransTime(tx.trans_time())
            });
        }
        writer.flush();
        writer.close();
    }

    private void exportExcel(
            List<TransactionResponse> txs, Map<Long, String> categoryMap, HttpServletResponse response, String filename)
            throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("transactions");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < CSV_HEADERS.length; i++) {
            headerRow.createCell(i).setCellValue(CSV_HEADERS[i]);
        }

        for (int i = 0; i < txs.size(); i++) {
            TransactionResponse tx = txs.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(formatType(tx.type()));
            row.createCell(1).setCellValue(formatAmount(tx.amount()));
            row.createCell(2)
                    .setCellValue(tx.category_id() != null ? categoryMap.getOrDefault(tx.category_id(), "") : "");
            row.createCell(3).setCellValue(tx.account_id() != null ? tx.account_id() : 0);
            row.createCell(4).setCellValue(tx.to_account_id() != null ? tx.to_account_id() : 0);
            row.createCell(5).setCellValue(SanitizeUtils.sanitizeCsvCell(tx.remark() != null ? tx.remark() : ""));
            row.createCell(6).setCellValue(formatTransTime(tx.trans_time()));
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void exportJson(
            List<TransactionResponse> txs, Map<Long, String> categoryMap, HttpServletResponse response, String filename)
            throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        List<Map<String, Object>> data = txs.stream()
                .map(tx -> {
                    Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("type", formatType(tx.type()));
                    map.put("amount", formatAmount(tx.amount()));
                    map.put("category", tx.category_id() != null ? categoryMap.getOrDefault(tx.category_id(), "") : "");
                    if (tx.account_id() != null) map.put("account_id", tx.account_id());
                    if (tx.to_account_id() != null) map.put("to_account_id", tx.to_account_id());
                    map.put("remark", SanitizeUtils.sanitizeCsvCell(tx.remark() != null ? tx.remark() : ""));
                    map.put("trans_time", formatTransTime(tx.trans_time()));
                    return map;
                })
                .toList();

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), data);
    }

    private List<TransactionRequest> parseCsv(MultipartFile file, Map<String, Long> nameToId, Map<Integer, Long> otherCategoryByType) throws Exception {
        List<TransactionRequest> items = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(reader)) {

            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new BizException("CSV 文件为空");
            }

            int idxType = findIndex(headers, "type");
            int idxAmount = findIndex(headers, "amount");
            int idxCategory = findIndex(headers, "category");
            if (idxCategory < 0) idxCategory = findIndex(headers, "category_id");
            int idxAccountId = findIndex(headers, "account_id");
            int idxToAccountId = findIndex(headers, "to_account_id");
            int idxRemark = findIndex(headers, "remark");
            int idxTransTime = findIndex(headers, "trans_time");

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                int type = parseType(parseString(row, idxType));
                items.add(new TransactionRequest(
                        null,
                        type,
                        parseAmount(parseString(row, idxAmount)),
                        parseLong(row, idxAccountId),
                        parseLong(row, idxToAccountId),
                        parseCategory(parseString(row, idxCategory), type, nameToId, otherCategoryByType),
                        parseString(row, idxRemark),
                        parseString(row, idxTransTime)));
            }
        }
        return items;
    }

    private List<TransactionRequest> parseExcel(MultipartFile file, Map<String, Long> nameToId, Map<Integer, Long> otherCategoryByType) throws Exception {
        List<TransactionRequest> items = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BizException("Excel 文件为空");
            }

            int idxType = findExcelIndex(headerRow, "type");
            int idxAmount = findExcelIndex(headerRow, "amount");
            int idxCategory = findExcelIndex(headerRow, "category");
            if (idxCategory < 0) idxCategory = findExcelIndex(headerRow, "category_id");
            int idxAccountId = findExcelIndex(headerRow, "account_id");
            int idxToAccountId = findExcelIndex(headerRow, "to_account_id");
            int idxRemark = findExcelIndex(headerRow, "remark");
            int idxTransTime = findExcelIndex(headerRow, "trans_time");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                int type = parseType(getCellString(row, idxType));
                items.add(new TransactionRequest(
                        null,
                        type,
                        parseAmount(getCellString(row, idxAmount)),
                        getCellLong(row, idxAccountId),
                        getCellLong(row, idxToAccountId),
                        parseCategory(getCellString(row, idxCategory), type, nameToId, otherCategoryByType),
                        getCellString(row, idxRemark),
                        getCellString(row, idxTransTime)));
            }
        }
        return items;
    }

    private List<TransactionRequest> parseJson(MultipartFile file, Map<String, Long> nameToId, Map<Integer, Long> otherCategoryByType) throws Exception {
        List<Map<String, Object>> rawList = objectMapper.readValue(
                file.getInputStream(), objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

        List<TransactionRequest> items = new ArrayList<>();
        for (Map<String, Object> map : rawList) {
            Object typeVal = map.get("type");
            Object amountVal = map.get("amount");
            Object categoryVal = map.getOrDefault("category", map.get("category_id"));
            int type = parseType(typeVal != null ? typeVal.toString() : "");

            items.add(new TransactionRequest(
                    null,
                    type,
                    parseAmount(amountVal != null ? amountVal.toString() : ""),
                    toLong(map.get("account_id")),
                    toLong(map.get("to_account_id")),
                    parseCategory(categoryVal != null ? categoryVal.toString() : "", type, nameToId, otherCategoryByType),
                    map.get("remark") != null ? map.get("remark").toString() : "",
                    map.get("trans_time") != null ? map.get("trans_time").toString() : ""));
        }
        return items;
    }

    private int findIndex(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private int findExcelIndex(Row headerRow, String name) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String cellValue = getCellString(headerRow, i);
            if (cellValue.trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private Integer parseInt(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        String val = row[idx].trim();
        if (val.isEmpty()) return null;
        return Integer.parseInt(val);
    }

    private Long parseLong(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        String val = row[idx].trim();
        if (val.isEmpty()) return null;
        return Long.parseLong(val);
    }

    private String parseString(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return null;
        return row[idx].trim();
    }

    private Integer getCellInt(Row row, int idx) {
        if (idx < 0 || row.getCell(idx) == null) return null;
        return (int) row.getCell(idx).getNumericCellValue();
    }

    private Long getCellLong(Row row, int idx) {
        if (idx < 0 || row.getCell(idx) == null) return null;
        return (long) row.getCell(idx).getNumericCellValue();
    }

    private String getCellString(Row row, int idx) {
        if (idx < 0 || row.getCell(idx) == null) return "";
        var cell = row.getCell(idx);
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> cell.toString();
        };
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    public void downloadTemplate(String format, HttpServletResponse response) {
        String filename = "import_template." + format;
        response.setCharacterEncoding("UTF-8");

        try {
            switch (format) {
                case "csv" -> {
                    response.setContentType("text/csv;charset=UTF-8");
                    response.setHeader("Content-Disposition",
                            "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
                    CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
                    writer.writeNext(CSV_HEADERS);
                    writer.writeNext(new String[] {"支出", "25.50", "餐饮", "1", "", "午餐", "2025-01-15 12:30:00"});
                    writer.flush();
                    writer.close();
                }
                case "excel" -> {
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response.setHeader("Content-Disposition",
                            "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
                    Workbook workbook = new XSSFWorkbook();
                    Sheet sheet = workbook.createSheet("import_template");
                    Row headerRow = sheet.createRow(0);
                    for (int i = 0; i < CSV_HEADERS.length; i++) {
                        headerRow.createCell(i).setCellValue(CSV_HEADERS[i]);
                    }
                    Row exampleRow = sheet.createRow(1);
                    exampleRow.createCell(0).setCellValue("支出");
                    exampleRow.createCell(1).setCellValue("25.50");
                    exampleRow.createCell(2).setCellValue("餐饮");
                    exampleRow.createCell(3).setCellValue(1);
                    exampleRow.createCell(4).setCellValue("");
                    exampleRow.createCell(5).setCellValue("午餐");
                    exampleRow.createCell(6).setCellValue("2025-01-15 12:30:00");
                    workbook.write(response.getOutputStream());
                    workbook.close();
                }
                case "json" -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setHeader("Content-Disposition",
                            "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
                    List<Map<String, Object>> example = List.of(Map.of(
                            "type", "支出",
                            "amount", "25.50",
                            "category", "餐饮",
                            "account_id", 1,
                            "to_account_id", "",
                            "remark", "午餐",
                            "trans_time", "2025-01-15 12:30:00"));
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), example);
                }
                default -> throw new BizException("不支持的模板格式: " + format);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("模板下载失败", e);
        }
    }

    private Transaction toEntity(TransactionRequest req) {
        Transaction tx = new Transaction();
        tx.setBookId(req.book_id());
        tx.setType(req.type());
        tx.setAmount(req.amount());
        tx.setAccountId(req.account_id());
        tx.setToAccountId(req.to_account_id());
        tx.setCategoryId(req.category_id());
        tx.setRemark(req.remark() != null ? SanitizeUtils.escapeHtml(req.remark()) : "");
        if (req.trans_time() != null && !req.trans_time().isBlank()) {
            tx.setTransTime(
                    LocalDateTime.parse(req.trans_time().replace(" ", "T").substring(0, 19)));
        } else {
            tx.setTransTime(LocalDateTime.now());
        }
        return tx;
    }
}
