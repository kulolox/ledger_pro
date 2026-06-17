package com.example.back_end.service.impl;

import com.example.back_end.entity.TokenBlacklist;
import com.example.back_end.mapper.TokenBlacklistMapper;
import com.example.back_end.service.ITokenBlacklistService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

public class DbTokenBlacklistService implements ITokenBlacklistService {
    private final TokenBlacklistMapper mapper;

    public DbTokenBlacklistService(TokenBlacklistMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void blacklist(String token, long ttlSeconds) {
        if (ttlSeconds <= 0) return;
        TokenBlacklist entry = new TokenBlacklist();
        entry.setTokenHash(sha256(token));
        entry.setExpiresAt(LocalDateTime.now().plusSeconds(ttlSeconds));
        mapper.insert(entry);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return mapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TokenBlacklist>()
                                .eq(TokenBlacklist::getTokenHash, sha256(token))
                                .gt(TokenBlacklist::getExpiresAt, LocalDateTime.now()))
                > 0;
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
