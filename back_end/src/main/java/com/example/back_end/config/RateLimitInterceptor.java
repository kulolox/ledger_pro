package com.example.back_end.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redis;

    public RateLimitInterceptor(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        int maxAttempts;
        String prefix;

        if (path.contains("/auth/login")) {
            maxAttempts = 10;
            prefix = "rate:login:";
        } else if (path.contains("/auth/register")) {
            maxAttempts = 5;
            prefix = "rate:register:";
        } else if (path.contains("/auth/send-register-code")) {
            maxAttempts = 5;
            prefix = "rate:reg-code:";
        } else if (path.contains("/auth/send-reset-code")) {
            maxAttempts = 3;
            prefix = "rate:reset-code:";
        } else if (path.contains("/auth/reset-password")) {
            maxAttempts = 5;
            prefix = "rate:reset-pwd:";
        } else if (path.contains("/auth/captcha")) {
            maxAttempts = 30;
            prefix = "rate:captcha:";
        } else {
            return true;
        }

        // 滑动计数：Redis INCR + 1 分钟过期，超过阈值返回 429
        String key = prefix + clientIp;
        Long count;
        try {
            count = redis.opsForValue().increment(key);
            if (count != null && count == 1) {
                redis.expire(key, Duration.ofMinutes(1));
            }
        } catch (Exception e) {
            // Redis 故障时放行，保证可用性（限流失效但记录告警）
            log.error("限流计数失败，本次放行 path={} ip={}", path, clientIp, e);
            return true;
        }

        if (count != null && count > maxAttempts) {
            log.warn("触发限流 path={} ip={} count={} max={}", path, clientIp, count, maxAttempts);
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"操作过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}
