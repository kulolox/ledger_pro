package com.example.back_end.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.back_end.common.Result;
import com.example.back_end.controller.AuthController;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.CaptchaResponse;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.service.IAuthService;
import com.example.back_end.utils.RsaUtils;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private IAuthService authService;

    @Mock
    private RsaUtils rsaUtils;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService, rsaUtils);
    }

    @Test
    @DisplayName("captcha: 成功返回图形验证码")
    void captcha_success() {
        when(authService.getCaptcha()).thenReturn(new CaptchaResponse("key-1", "data:image/png;base64,xxxx"));

        Result<CaptchaResponse> result = controller.captcha();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("key-1", result.getData().captchaKey());
        assertEquals("data:image/png;base64,xxxx", result.getData().captchaBase64());
    }

    @Test
    @DisplayName("sendRegisterCode: 成功发送注册验证码")
    void sendRegisterCode_success() {
        Map<String, String> params = Map.of(
                "email", "user@test.com",
                "captchaKey", "key-1",
                "captchaCode", "abcd");

        Result<Map<String, String>> result = controller.sendRegisterCode(params);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("验证码已发送", result.getMessage());
        verify(authService).sendRegisterCode("user@test.com", "key-1", "abcd");
    }

    @Test
    @DisplayName("sendResetCode: 支持携带图形码发送重置验证码")
    void sendResetCode_withCaptcha_success() {
        Map<String, String> params = Map.of(
                "email", "user@test.com",
                "captchaKey", "key-2",
                "captchaCode", "efgh");

        Result<Map<String, String>> result = controller.sendResetCode(params);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(authService).sendResetCode("user@test.com", "key-2", "efgh");
    }

    @Test
    @DisplayName("register: 成功注册用户")
    void register_success() {
        RegisterRequest req = new RegisterRequest("user@test.com", "encPwd", "测试用户", "123456");
        UserInfoResponse userInfo = new UserInfoResponse(1L, "测试用户", "", "user@test.com", "");
        LoginResponse loginResp = new LoginResponse("token", "refreshToken", userInfo);

        when(authService.register(req)).thenReturn(loginResp);

        Result<LoginResponse> result = controller.register(req);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("注册成功", result.getMessage());
        assertEquals("token", result.getData().token());
    }
}
