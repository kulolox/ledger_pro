package com.example.back_end.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务预期异常（参数错误/无权限等）：warn + 原样返回，不当作系统错误
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.warn("BizException: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("DuplicateKeyException: {}", e.getMessage());
        return Result.fail(400, "数据已存在或邮箱已被注册");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        log.error("RuntimeException", e);
        return Result.fail(400, "请求处理异常");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Result.fail(500, "服务器内部错误");
    }
}
