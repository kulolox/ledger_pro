package com.example.back_end.service;

public interface ITokenBlacklistService {
    void blacklist(String token, long ttlSeconds);

    boolean isBlacklisted(String token);
}
