package com.example.back_end.service.impl;

import com.example.back_end.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public MailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    public void sendResetCode(String toEmail, String code) {
        // SMTP 为外部依赖，失败转业务异常给用户友好提示（原始异常记日志便于排查）
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject("LedgerPro 密码重置验证码");
            msg.setText("您的验证码为：" + code + "，有效期5分钟。如非本人操作，请忽略。");
            mailSender.send(msg);
            log.info("重置验证码邮件发送成功 toEmail={}", toEmail);
        } catch (Exception e) {
            log.error("重置验证码邮件发送失败 toEmail={}", toEmail, e);
            throw new BizException("验证码发送失败，请稍后再试");
        }
    }

    public void sendRegisterCode(String toEmail, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject("LedgerPro 账号注册验证码");
            msg.setText("您正在注册 LedgerPro 账号，验证码为：" + code + "，有效期10分钟。如非本人操作，请忽略此邮件。");
            mailSender.send(msg);
            log.info("注册验证码邮件发送成功 toEmail={}", toEmail);
        } catch (Exception e) {
            log.error("注册验证码邮件发送失败 toEmail={}", toEmail, e);
            throw new BizException("验证码发送失败，请稍后再试");
        }
    }
}
