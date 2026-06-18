import { test, expect } from "../fixtures/test.fixture";
import { ImportExportPage } from "../page-objects/ImportExportPage";
import {
  createTestTransactions,
  getCategoryIds,
  cleanupTestTransactions,
  exportTransactionsApi,
  importTransactionsApi,
  getTransactionCount,
  getLatestTransactionCategory,
} from "../utils/db";

test.describe("Import Export", () => {
  test("import export page renders", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await expect(iePage.exportTab).toBeVisible();
    await expect(iePage.importTab).toBeVisible();
    await expect(iePage.exportBtn).toBeVisible();
  });

  test("switch between export and import tabs", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await expect(iePage.exportBtn).toBeVisible();

    await iePage.switchToImport();
    await authenticatedPage.waitForTimeout(300);
    await expect(iePage.importBtn).toBeVisible();
    await expect(iePage.fileArea).toBeVisible();

    await iePage.switchToExport();
    await authenticatedPage.waitForTimeout(300);
    await expect(iePage.exportBtn).toBeVisible();
  });

  test("export format selection", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.selectFormat("excel");
    await authenticatedPage.waitForTimeout(200);
    const excelChip = iePage.formatChips.filter({ hasText: "Excel" });
    await expect(excelChip).toHaveClass(/active/);

    await iePage.selectFormat("csv");
    const csvChip = iePage.formatChips.filter({ hasText: "CSV" });
    await expect(csvChip).toHaveClass(/active/);
  });

  test("import tab shows file picker area", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToImport();

    await expect(iePage.fileArea).toBeVisible();
    const fileHint = await iePage.fileArea.textContent();
    expect(fileHint).toContain("点击选择文件");
  });

  test("import button disabled without file", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToImport();

    await expect(iePage.importBtn).toHaveClass(/disabled/);
  });

  test("back button returns to home", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.clickBack();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("export has year/month mode toggle", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await expect(iePage.modeChips).toHaveCount(2);
    await expect(iePage.monthPicker).toBeVisible();
    await expect(iePage.yearPicker).not.toBeVisible();
  });

  test("switch to year mode shows year picker", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.switchToMode("year");
    await authenticatedPage.waitForTimeout(300);

    await expect(iePage.monthPicker).not.toBeVisible();
    await expect(iePage.yearPicker).toBeVisible();
  });

  test("year picker changes year on arrow click", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToMode("year");
    await authenticatedPage.waitForTimeout(300);

    const initialYear = await iePage.yearText.textContent();

    await iePage.clickPrevYear();
    await authenticatedPage.waitForTimeout(200);
    const prevYear = await iePage.yearText.textContent();
    expect(prevYear).not.toBe(initialYear);

    await iePage.clickNextYear();
    await authenticatedPage.waitForTimeout(200);
    const nextYear = await iePage.yearText.textContent();
    expect(nextYear).toBe(initialYear);
  });

  test("import format chips in import tab", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToImport();
    await authenticatedPage.waitForTimeout(300);

    await expect(iePage.formatChips.first()).toBeVisible();
  });

  test("export CSV with data returns valid content", async ({ authenticatedPage, testUser }) => {
    const categoryIds = await getCategoryIds(testUser.userId);
    await createTestTransactions(testUser.bookId, testUser.accountId, categoryIds);

    try {
      const count = await getTransactionCount(testUser.bookId);
      expect(count).toBe(3);

      const token = await authenticatedPage.evaluate(() => localStorage.getItem("token") || "");

      const { status, body, contentType } = await exportTransactionsApi(
        testUser.bookId,
        { month: "2025-01", format: "csv" },
        token,
      );

      expect(status).toBe(200);
      expect(contentType).toContain("text/csv");

      const lines = body.trim().split("\n");
      expect(lines.length).toBeGreaterThanOrEqual(2);

      const header = lines[0];
      expect(header).toContain("type");
      expect(header).toContain("amount");
      expect(header).toContain("category");

      const dataLines = lines.slice(1);
      expect(dataLines.length).toBe(3);
    } finally {
      await cleanupTestTransactions(testUser.bookId);
    }
  });

  test("export JSON with data returns valid content", async ({ authenticatedPage, testUser }) => {
    const categoryIds = await getCategoryIds(testUser.userId);
    await createTestTransactions(testUser.bookId, testUser.accountId, categoryIds);

    try {
      const count = await getTransactionCount(testUser.bookId);
      expect(count).toBe(3);

      const token = await authenticatedPage.evaluate(() => localStorage.getItem("token") || "");

      const { status, body } = await exportTransactionsApi(
        testUser.bookId,
        { month: "2025-01", format: "json" },
        token,
      );

      expect(status).toBe(200);
      const data = JSON.parse(body);
      expect(Array.isArray(data)).toBe(true);
      expect(data.length).toBe(3);

      const first = data[0];
      expect(first).toHaveProperty("type");
      expect(first).toHaveProperty("amount");
      expect(first).toHaveProperty("category");
    } finally {
      await cleanupTestTransactions(testUser.bookId);
    }
  });

  test("export year mode returns all 12 months data", async ({ authenticatedPage, testUser }) => {
    const categoryIds = await getCategoryIds(testUser.userId);
    await createTestTransactions(testUser.bookId, testUser.accountId, categoryIds);

    try {
      const token = await authenticatedPage.evaluate(() => localStorage.getItem("token") || "");

      const { status, body } = await exportTransactionsApi(
        testUser.bookId,
        { year: "2025", format: "json" },
        token,
      );

      expect(status).toBe(200);
      const data = JSON.parse(body);
      expect(data.length).toBeGreaterThanOrEqual(3);
    } finally {
      await cleanupTestTransactions(testUser.bookId);
    }
  });

  test("import CSV inserts data into database", async ({ authenticatedPage, testUser }) => {
    const token = await authenticatedPage.evaluate(() => localStorage.getItem("token") || "");

    const csvContent =
      "type,amount,category,account_id,to_account_id,remark,trans_time\n" +
      `支出,99.50,餐饮,${testUser.accountId},,测试导入午餐,2025-02-10 12:00:00\n` +
      `收入,3000.00,工资,${testUser.accountId},,测试导入工资,2025-02-10 09:00:00`;

    let imported = 0;
    try {
      const { status, body } = await importTransactionsApi(
        testUser.bookId,
        "csv",
        csvContent,
        token,
      );

      expect(status).toBe(200);
      expect(body.code).toBe(200);
      expect(body.data.imported).toBe(2);
      expect(body.data.total).toBe(2);
      imported = body.data.imported;

      const dbCount = await getTransactionCount(testUser.bookId);
      expect(dbCount).toBe(2);
    } finally {
      if (imported > 0) await cleanupTestTransactions(testUser.bookId);
    }
  });

  test("import CSV with unknown category falls back to 其他", async ({ authenticatedPage, testUser }) => {
    const categoryIds = await getCategoryIds(testUser.userId);
    const token = await authenticatedPage.evaluate(() => localStorage.getItem("token") || "");

    const csvContent =
      "type,amount,category,account_id,to_account_id,remark,trans_time\n" +
      `支出,50.00,完全不存在的分类,${testUser.accountId},,未知分类测试,2025-03-01 10:00:00`;

    let imported = 0;
    try {
      const { status, body } = await importTransactionsApi(
        testUser.bookId,
        "csv",
        csvContent,
        token,
      );

      expect(status).toBe(200);
      expect(body.code).toBe(200);
      expect(body.data.imported).toBe(1);
      expect(body.data.errors).toHaveLength(0);
      imported = body.data.imported;

      const dbCount = await getTransactionCount(testUser.bookId);
      expect(dbCount).toBe(1);

      const categoryId = await getLatestTransactionCategory(testUser.bookId);
      expect(categoryId).toBe(categoryIds.other);
    } finally {
      if (imported > 0) await cleanupTestTransactions(testUser.bookId);
    }
  });

  test("import CSV with empty category falls back to 其他", async ({ authenticatedPage, testUser }) => {
    const categoryIds = await getCategoryIds(testUser.userId);
    const token = await authenticatedPage.evaluate(() => localStorage.getItem("token") || "");

    const csvContent =
      "type,amount,category,account_id,to_account_id,remark,trans_time\n" +
      `支出,30.00,,${testUser.accountId},,空分类测试,2025-03-05 14:00:00`;

    let imported = 0;
    try {
      const { status, body } = await importTransactionsApi(
        testUser.bookId,
        "csv",
        csvContent,
        token,
      );

      expect(status).toBe(200);
      expect(body.code).toBe(200);
      expect(body.data.imported).toBe(1);
      expect(body.data.errors).toHaveLength(0);
      imported = body.data.imported;

      const dbCount = await getTransactionCount(testUser.bookId);
      expect(dbCount).toBe(1);

      const categoryId = await getLatestTransactionCategory(testUser.bookId);
      expect(categoryId).toBe(categoryIds.other);
    } finally {
      if (imported > 0) await cleanupTestTransactions(testUser.bookId);
    }
  });
});
