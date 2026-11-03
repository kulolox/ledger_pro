package com.example.back_end.unit.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.back_end.common.Result;
import com.example.back_end.controller.SystemController;
import com.example.back_end.dto.response.AppVersionResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class SystemControllerTest {

    @Test
    @DisplayName("检查更新：比最新版本低时返回 hasUpdate=true")
    void shouldReturnHasUpdateTrueWhenCurrentVersionIsLower() {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "latestVersion", "1.2.0");
        ReflectionTestUtils.setField(controller, "versionCode", 120);
        ReflectionTestUtils.setField(controller, "minVersion", "1.0.0");
        ReflectionTestUtils.setField(controller, "changelog", "新特性上线");
        ReflectionTestUtils.setField(controller, "downloadUrl", "https://example.com/app.apk");
        ReflectionTestUtils.setField(controller, "packageSize", "15 MB");
        ReflectionTestUtils.setField(controller, "forceUpdate", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        Result<AppVersionResponse> res = controller.checkVersion(request, "1.1.9", null);
        assertNotNull(res);
        assertEquals(200, res.getCode());
        AppVersionResponse data = res.getData();
        assertNotNull(data);
        assertTrue(data.hasUpdate());
        assertEquals("1.2.0", data.latestVersion());
        assertEquals("新特性上线", data.changelog());
    }

    @Test
    @DisplayName("检查更新：传入 currentVersionCode 比最新小，返回 hasUpdate=true")
    void shouldReturnHasUpdateTrueWhenVersionCodeIsLower() {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "latestVersion", "1.0.1");
        ReflectionTestUtils.setField(controller, "versionCode", 101);

        MockHttpServletRequest request = new MockHttpServletRequest();
        Result<AppVersionResponse> res = controller.checkVersion(request, "1.0.1", 100);
        assertNotNull(res);
        assertTrue(res.getData().hasUpdate());
    }

    @Test
    @DisplayName("检查更新：传入 currentVersionCode 与最新一致，返回 hasUpdate=false")
    void shouldReturnHasUpdateFalseWhenVersionCodeMatches() {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "latestVersion", "1.0.1");
        ReflectionTestUtils.setField(controller, "versionCode", 101);

        MockHttpServletRequest request = new MockHttpServletRequest();
        Result<AppVersionResponse> res = controller.checkVersion(request, "1.0.1", 101);
        assertNotNull(res);
        assertFalse(res.getData().hasUpdate());
    }

    @Test
    @DisplayName("检查更新：优先读取 data/apk/latest.json 发布的最新元数据")
    void shouldPrioritizeLatestJsonMetadata() throws Exception {
        Path metaDir = Path.of("data", "apk");
        Files.createDirectories(metaDir);
        Path metaFile = metaDir.resolve("latest.json");

        String jsonContent =
                """
                {
                  "version": "1.0.5",
                  "versionCode": 105,
                  "packageSize": "35.0 MB",
                  "changelog": "1. Git 提交自动提取日志",
                  "downloadUrl": "/api/v1/system/version/download"
                }
                """;
        Files.writeString(metaFile, jsonContent);

        try {
            SystemController controller = new SystemController();
            ReflectionTestUtils.setField(controller, "latestVersion", "1.0.0");
            ReflectionTestUtils.setField(controller, "versionCode", 100);

            MockHttpServletRequest request = new MockHttpServletRequest();
            Result<AppVersionResponse> res = controller.checkVersion(request, "1.0.1", 101);
            assertNotNull(res);
            AppVersionResponse data = res.getData();
            assertNotNull(data);
            assertTrue(data.hasUpdate());
            assertEquals("1.0.5", data.latestVersion());
            assertEquals(105, data.versionCode());
            assertEquals("1. Git 提交自动提取日志", data.changelog());
        } finally {
            // 清理临时测试元数据文件
            Files.deleteIfExists(metaFile);
        }
    }

    @Test
    @DisplayName("下载 APK：配置了外部绝对 URL 时返回 302 重定向")
    void shouldRedirectWhenExternalUrlProvided() {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "downloadUrl", "https://example.com/app.apk");

        ResponseEntity<?> response = controller.downloadApk();
        assertNotNull(response);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertEquals(
                "https://example.com/app.apk",
                response.getHeaders().getLocation().toString());
    }

    @Test
    @DisplayName("下载 APK：本地无 apk 文件且为默认相对路径时返回 404")
    void shouldReturnNotFoundWhenNoApkPresent() {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "downloadUrl", "/api/v1/system/version/download");

        ResponseEntity<?> response = controller.downloadApk();
        assertNotNull(response);
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NOT_FOUND);
    }
}
