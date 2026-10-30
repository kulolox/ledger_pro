package com.example.back_end.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private final SecretKey secretKey;
    private final long accessExpireMs = 2 * 60 * 60 * 1000L; // 2小时短Token
    private final long refreshExpireMs = 7 * 24 * 60 * 60 * 1000L;

    // 从配置文件（application.yml）读取 app.jwt.secret 的值
    public JwtUtils(@Value("${app.jwt.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET 环境变量未配置，请在启动前设置 JWT_SECRET");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] finalBytes = keyBytes.length < 32 ? Arrays.copyOf(keyBytes, 32) : keyBytes;
        this.secretKey = Keys.hmacShaKeyFor(finalBytes);
    }

    public String generateToken(Long userId, String email) {
        return buildToken(userId, email, "access", accessExpireMs);
    }

    public String generateRefreshToken(Long userId, String email) {
        return buildToken(userId, email, "refresh", refreshExpireMs);
    }

    // type claim 区分 access/refresh：validateToken/validateRefreshToken 据此防止类型混淆（refresh 不能当 access 用）
    private String buildToken(Long userId, String email, String type, long expireMs) {
        return Jwts.builder()
                .subject(email) // 设置 JWT 的 主体（Subject），通常存放用户标识（这里存邮箱）
                .claim("userId", userId) // 自定义声明，存放用户 ID
                .claim("type", type) // 自定义声明，区分 token 类型
                .issuedAt(new Date()) // 签发时间（当前时间）
                .expiration(new Date(System.currentTimeMillis() + expireMs)) // 过期时间（当前时间 + 有效期）
                .signWith(secretKey) // 使用密钥对 JWT 进行签名（HMAC-SHA）
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public Claims parseClaimsIfValid(String token) {
        try {
            Claims claims = parseClaims(token);
            if ("access".equals(claims.get("type", String.class))) {
                return claims;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "access".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // 解析token 获取相关信息（jjwt 内置验签 + 过期校验，失败抛异常由调用方 catch）
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
