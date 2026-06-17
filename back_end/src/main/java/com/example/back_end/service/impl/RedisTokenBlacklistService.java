package com.example.back_end.service.impl;

import com.example.back_end.service.ITokenBlacklistService;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisTokenBlacklistService implements ITokenBlacklistService {
    private static final String PREFIX = "blacklist:token:";
    private final StringRedisTemplate redis;

    public RedisTokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void blacklist(String token, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redis.opsForValue().set(PREFIX + token, "1", Duration.ofSeconds(ttlSeconds));
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
    }
}
