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
        } else if (path.contains("/auth/send-reset-code")) {
            maxAttempts = 3;
            prefix = "rate:reset-code:";
        } else if (path.contains("/auth/reset-password")) {
            maxAttempts = 5;
            prefix = "rate:reset-pwd:";
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
            // Redis 故障时放行，保证登录可用性（限流失效但记录告警）
            log.error("限流计数失败，本次放行 path={} ip={}", path, clientIp, e);
            return true;
        }

        if (count != null && count > maxAttempts) {
            log.warn("触发限流 path={} ip={} count={} max={}", path, clientIp, count, maxAttempts);
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        // 仅当显式配置 TRUST_PROXY_HEADERS=true 才信任代理头，防止客户端伪造 IP 绕过限流
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && "true".equals(System.getenv("TRUST_PROXY_HEADERS"))) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank() && "true".equals(System.getenv("TRUST_PROXY_HEADERS"))) {
            return xri;
        }
        return request.getRemoteAddr();
    }
}
