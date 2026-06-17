package com.example.back_end.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

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
        } else if (path.contains("/auth/send-reset-code")) {
            maxAttempts = 3;
            prefix = "rate:reset-code:";
        } else if (path.contains("/auth/reset-password")) {
            maxAttempts = 5;
            prefix = "rate:reset-pwd:";
        } else {
            return true;
        }

        String key = prefix + clientIp;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(1));
        }

        if (count != null && count > maxAttempts) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri;
        }
        return request.getRemoteAddr();
    }
}
