package com.example.back_end.service;

public interface IVerificationCodeService {

    /**
     * 保存图形验证码（通常 TTL = 2分钟）
     */
    void saveCaptcha(String captchaKey, String code, long ttlSeconds);

    /**
     * 校验并核销图形验证码（一次性失效，比对不区分大小写）
     */
    boolean verifyAndInvalidateCaptcha(String captchaKey, String code);

    /**
     * 保存注册邮箱验证码（通常 TTL = 10分钟）
     */
    void saveRegisterCode(String email, String code, long ttlSeconds);

    /**
     * 校验并核销注册邮箱验证码（一次性失效）
     */
    boolean verifyAndInvalidateRegisterCode(String email, String code);

    /**
     * 检查邮箱发信是否处于冷却中（如 60 秒冷却）
     */
    boolean isRegisterCooldown(String email);

    /**
     * 设置发信冷却时间（如 60 秒）
     */
    void setRegisterCooldown(String email, long cooldownSeconds);
}
