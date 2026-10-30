package com.example.back_end.unit.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.back_end.common.BizException;
import com.example.back_end.common.GlobalExceptionHandler;
import com.example.back_end.common.Result;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleBiz: 正确返回业务异常的状态码和消息")
    void handleBiz_returnsCodeAndMessage() {
        BizException ex = new BizException(401, "未登录或登录过期");
        Result<Void> res = handler.handleBiz(ex);
        assertEquals(401, res.getCode());
        assertEquals("未登录或登录过期", res.getMessage());
        assertNull(res.getData());
    }

    @Test
    @DisplayName("handleValidation: 拼接字段错误信息并返回400")
    void handleValidation_formatsFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError f1 = new FieldError("req", "email", "邮箱不能为空");
        FieldError f2 = new FieldError("req", "password", "密码不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(f1, f2));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        Result<Void> res = handler.handleValidation(ex);
        assertEquals(400, res.getCode());
        assertTrue(res.getMessage().contains("email: 邮箱不能为空"));
        assertTrue(res.getMessage().contains("password: 密码不能为空"));
    }

    @Test
    @DisplayName("handleDuplicateKey: 数据库唯一键冲突返回400友好提示")
    void handleDuplicateKey_returns400() {
        DuplicateKeyException ex = new DuplicateKeyException("Duplicate entry 'test@test.com' for key 'uk_email'");
        Result<Void> res = handler.handleDuplicateKey(ex);
        assertEquals(400, res.getCode());
        assertEquals("数据已存在或邮箱已被注册", res.getMessage());
    }

    @Test
    @DisplayName("handleRuntime: 未知运行时异常返回400")
    void handleRuntime_returns400() {
        RuntimeException ex = new RuntimeException("DB Connection timeout");
        Result<Void> res = handler.handleRuntime(ex);
        assertEquals(400, res.getCode());
        assertEquals("请求处理异常", res.getMessage());
    }

    @Test
    @DisplayName("handleException: 未捕获检查异常返回500")
    void handleException_returns500() {
        Exception ex = new Exception("Fatal system error");
        Result<Void> res = handler.handleException(ex);
        assertEquals(500, res.getCode());
        assertEquals("服务器内部错误", res.getMessage());
    }
}
