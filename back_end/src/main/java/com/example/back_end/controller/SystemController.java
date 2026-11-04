package com.example.back_end.controller;

import com.example.back_end.common.Result;
import com.example.back_end.dto.response.AppVersionResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemController {

    private static final Pattern LANZOU_PWD_PATTERN = Pattern.compile("【蓝奏云提取码[:：\\s]*([a-zA-Z0-9]+)】");

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.update.latest-version:1.0.1}")
    private String latestVersion;

    @Value("${app.update.version-code:101}")
    private Integer versionCode;

    @Value("${app.update.min-version:1.0.0}")
    private String minVersion;

    @Value("${app.update.changelog:1. 优化分类拖拽排序与交互体验\\n2. 新增检查更新与软件包下载功能\\n3. 修复已知异常，提升系统性能}")
    private String changelog;

    @Value("${app.update.download-url:}")
    private String downloadUrl;

    @Value("${app.update.package-size:}")
    private String packageSize;

    @Value("${app.update.force-update:false}")
    private boolean forceUpdate;

    @Value("${app.update.lanzou-url:}")
    private String lanzouUrl;

    @Value("${app.update.lanzou-pwd:}")
    private String lanzouPwd;

    @Value("${app.update.github-url:}")
    private String githubUrl;

    @Value("${app.update.meta-file:data/apk/latest.json}")
    private String metaFilePath = "data/apk/latest.json";

    @GetMapping("/version/check")
    public Result<AppVersionResponse> checkVersion(
            HttpServletRequest request,
            @RequestParam(value = "currentVersion", required = false) String currentVersion,
            @RequestParam(value = "currentVersionCode", required = false) Integer currentVersionCode) {

        ApkReleaseMetadata meta = readReleaseMetadata();

        String effectiveLatestVersion =
                (meta != null && meta.version() != null && !meta.version().isBlank()) ? meta.version() : latestVersion;

        Integer effectiveVersionCode = (meta != null && meta.versionCode() != null && meta.versionCode() > 0)
                ? meta.versionCode()
                : versionCode;

        String effectiveChangelog =
                (meta != null && meta.changelog() != null && !meta.changelog().isBlank())
                        ? meta.changelog()
                        : changelog;

        String effectivePackageSize = (meta != null
                        && meta.packageSize() != null
                        && !meta.packageSize().isBlank())
                ? meta.packageSize()
                : packageSize;

        String effectiveDownloadUrl = (meta != null
                        && meta.downloadUrl() != null
                        && !meta.downloadUrl().isBlank())
                ? meta.downloadUrl()
                : downloadUrl;

        String effectiveLanzouUrl =
                (meta != null && meta.lanzouUrl() != null && !meta.lanzouUrl().isBlank())
                        ? meta.lanzouUrl()
                        : lanzouUrl;

        String effectiveLanzouPwd =
                (meta != null && meta.lanzouPwd() != null && !meta.lanzouPwd().isBlank())
                        ? meta.lanzouPwd()
                        : lanzouPwd;

        String effectiveGithubUrl =
                (meta != null && meta.githubUrl() != null && !meta.githubUrl().isBlank())
                        ? meta.githubUrl()
                        : githubUrl;

        // 若 changelog 中包含提取码但 effectiveLanzouPwd 为空，自动解析兜底
        if ((effectiveLanzouPwd == null || effectiveLanzouPwd.isBlank()) && effectiveChangelog != null) {
            Matcher m = LANZOU_PWD_PATTERN.matcher(effectiveChangelog);
            if (m.find()) {
                effectiveLanzouPwd = m.group(1);
            }
        }

        boolean hasUpdate;
        if (currentVersionCode != null && currentVersionCode > 0 && effectiveVersionCode != null) {
            hasUpdate = effectiveVersionCode > currentVersionCode;
        } else {
            hasUpdate = isNewerVersion(effectiveLatestVersion, currentVersion);
        }

        File latestApk = findLatestApk(meta != null ? meta.apkFileName() : null);
        if ((effectivePackageSize == null || effectivePackageSize.isBlank()) && latestApk != null) {
            double mb = latestApk.length() / (1024.0 * 1024.0);
            effectivePackageSize = String.format(Locale.ROOT, "%.1f MB", mb);
        } else if (effectivePackageSize == null || effectivePackageSize.isBlank()) {
            effectivePackageSize = "";
        }

        // 智能补充 GitHub 原始直链（若未显式指定）
        if ((effectiveGithubUrl == null || effectiveGithubUrl.isBlank()) && effectiveLatestVersion != null) {
            String tag = effectiveLatestVersion.startsWith("v") ? effectiveLatestVersion : "v" + effectiveLatestVersion;
            String fileName = (meta != null
                            && meta.apkFileName() != null
                            && !meta.apkFileName().isBlank())
                    ? meta.apkFileName()
                    : (latestApk != null ? latestApk.getName() : ("LedgerPro-" + tag + "-prod-release.apk"));
            effectiveGithubUrl = "https://github.com/flycat0114/ledger_pro/releases/download/" + tag + "/" + fileName;
        }

        // 安全防刷保护：
        // 绝不对外向客户端暴露本服务器自身的下载链接，防止服务器带宽被恶意大流量刷爆或 DDoS
        // 默认将外部安全下载源（蓝奏云 / GitHub）作为默认 downloadUrl
        if (effectiveDownloadUrl == null
                || effectiveDownloadUrl.isBlank()
                || effectiveDownloadUrl.contains("/system/version/download")) {
            effectiveDownloadUrl = (effectiveLanzouUrl != null && !effectiveLanzouUrl.isBlank())
                    ? effectiveLanzouUrl
                    : effectiveGithubUrl;
        }

        return Result.ok(new AppVersionResponse(
                hasUpdate,
                effectiveLatestVersion,
                effectiveVersionCode,
                effectiveChangelog,
                effectiveDownloadUrl,
                effectivePackageSize,
                forceUpdate,
                effectiveLanzouUrl,
                effectiveLanzouPwd,
                effectiveGithubUrl));
    }

    /**
     * 服务器本地 APK 下载接口（保留在代码库中作为备份/内部兜底，不对外向用户主动公开暴露）
     */
    @GetMapping("/version/download")
    public ResponseEntity<?> downloadApk() {
        if (downloadUrl != null
                && (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://"))
                && !downloadUrl.contains("/system/version/download")) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(downloadUrl))
                    .build();
        }

        ApkReleaseMetadata meta = readReleaseMetadata();
        File latestApk = findLatestApk(meta != null ? meta.apkFileName() : null);
        if (latestApk == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.fail(404, "服务端未找到可用的 APK 安装包"));
        }

        Resource resource = new FileSystemResource(latestApk);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + latestApk.getName() + "\"")
                .contentLength(latestApk.length())
                .body(resource);
    }

    private ApkReleaseMetadata readReleaseMetadata() {
        if (metaFilePath == null || metaFilePath.isBlank()) {
            return null;
        }
        try {
            Path metaPath = Path.of(metaFilePath);
            File file = metaPath.toFile();
            if (file.exists() && file.isFile()) {
                ObjectMapper mapper = objectMapper != null ? objectMapper : new ObjectMapper();
                return mapper.readValue(file, ApkReleaseMetadata.class);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private File findLatestApk(String targetFileName) {
        Path apkDir = Path.of("data", "apk");
        File dir = apkDir.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        if (targetFileName != null && !targetFileName.isBlank()) {
            File target = new File(dir, targetFileName);
            if (target.exists() && target.isFile()) {
                return target;
            }
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".apk"));
        if (files == null || files.length == 0) {
            return null;
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files[0];
    }

    private boolean isNewerVersion(String latest, String current) {
        if (current == null || current.isBlank()) {
            return true;
        }
        String cleanLatest = latest.replaceFirst("^[vV]", "").trim();
        String cleanCurrent = current.replaceFirst("^[vV]", "").trim();
        if (cleanLatest.equals(cleanCurrent)) {
            return false;
        }
        String[] lParts = cleanLatest.split("\\.");
        String[] cParts = cleanCurrent.split("\\.");
        int length = Math.max(lParts.length, cParts.length);
        for (int i = 0; i < length; i++) {
            int l = i < lParts.length ? parsePart(lParts[i]) : 0;
            int c = i < cParts.length ? parsePart(cParts[i]) : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private int parsePart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApkReleaseMetadata(
            String version,
            Integer versionCode,
            String packageSize,
            String apkFileName,
            String downloadUrl,
            String lanzouUrl,
            String lanzouPwd,
            String githubUrl,
            String changelog,
            String buildTime) {}
}
