package com.example.back_end.service.impl;

import com.example.back_end.service.IVerificationCodeService;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class RedisVerificationCodeService implements IVerificationCodeService {

    private static final String CAPTCHA_PREFIX = "auth:captcha:";
    private static final String REG_CODE_PREFIX = "auth:reg:code:";
    private static final String REG_COOLDOWN_PREFIX = "auth:reg:cooldown:";

    private final StringRedisTemplate redis;

    public RedisVerificationCodeService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void saveCaptcha(String captchaKey, String code, long ttlSeconds) {
        if (captchaKey == null || code == null) return;
        String key = CAPTCHA_PREFIX + captchaKey;
        redis.opsForValue().set(key, code.toUpperCase(), Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean verifyAndInvalidateCaptcha(String captchaKey, String code) {
        if (captchaKey == null || code == null) return false;
        String key = CAPTCHA_PREFIX + captchaKey;
        String cachedCode = redis.opsForValue().get(key);
        // 一次性核销，无论比对成功与否立即删除该 key 防爆破
        redis.delete(key);
        return cachedCode != null && cachedCode.equalsIgnoreCase(code.trim());
    }

    @Override
    public void saveRegisterCode(String email, String code, long ttlSeconds) {
        if (email == null || code == null) return;
        String key = REG_CODE_PREFIX + email.toLowerCase().trim();
        redis.opsForValue().set(key, code.trim(), Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean verifyAndInvalidateRegisterCode(String email, String code) {
        if (email == null || code == null) return false;
        String key = REG_CODE_PREFIX + email.toLowerCase().trim();
        String cachedCode = redis.opsForValue().get(key);
        if (cachedCode != null && cachedCode.equals(code.trim())) {
            redis.delete(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRegisterCooldown(String email) {
        if (email == null) return false;
        String key = REG_COOLDOWN_PREFIX + email.toLowerCase().trim();
        Boolean hasKey = redis.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public void setRegisterCooldown(String email, long cooldownSeconds) {
        if (email == null) return;
        String key = REG_COOLDOWN_PREFIX + email.toLowerCase().trim();
        redis.opsForValue().set(key, "1", Duration.ofSeconds(cooldownSeconds));
    }
}
