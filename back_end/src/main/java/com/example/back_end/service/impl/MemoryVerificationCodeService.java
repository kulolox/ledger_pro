package com.example.back_end.service.impl;

import com.example.back_end.service.IVerificationCodeService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryVerificationCodeService implements IVerificationCodeService {

    private record ExpiringItem(String value, long expireAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, ExpiringItem> captchaMap = new ConcurrentHashMap<>();
    private final Map<String, ExpiringItem> regCodeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldownMap = new ConcurrentHashMap<>();

    @Override
    public void saveCaptcha(String captchaKey, String code, long ttlSeconds) {
        if (captchaKey == null || code == null) return;
        captchaMap.put(
                captchaKey, new ExpiringItem(code.toUpperCase(), System.currentTimeMillis() + ttlSeconds * 1000));
    }

    @Override
    public boolean verifyAndInvalidateCaptcha(String captchaKey, String code) {
        if (captchaKey == null || code == null) return false;
        ExpiringItem item = captchaMap.remove(captchaKey);
        if (item == null || item.isExpired()) {
            return false;
        }
        return item.value().equalsIgnoreCase(code.trim());
    }

    @Override
    public void saveRegisterCode(String email, String code, long ttlSeconds) {
        if (email == null || code == null) return;
        regCodeMap.put(
                email.toLowerCase().trim(),
                new ExpiringItem(code.trim(), System.currentTimeMillis() + ttlSeconds * 1000));
    }

    @Override
    public boolean verifyAndInvalidateRegisterCode(String email, String code) {
        if (email == null || code == null) return false;
        String key = email.toLowerCase().trim();
        ExpiringItem item = regCodeMap.get(key);
        if (item != null && !item.isExpired() && item.value().equals(code.trim())) {
            regCodeMap.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRegisterCooldown(String email) {
        if (email == null) return false;
        Long expireAt = cooldownMap.get(email.toLowerCase().trim());
        return expireAt != null && System.currentTimeMillis() <= expireAt;
    }

    @Override
    public void setRegisterCooldown(String email, long cooldownSeconds) {
        if (email == null) return;
        cooldownMap.put(email.toLowerCase().trim(), System.currentTimeMillis() + cooldownSeconds * 1000);
    }
}
