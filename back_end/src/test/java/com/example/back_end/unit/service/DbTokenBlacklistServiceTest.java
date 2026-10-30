package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back_end.entity.TokenBlacklist;
import com.example.back_end.mapper.TokenBlacklistMapper;
import com.example.back_end.service.impl.DbTokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DbTokenBlacklistService Unit Tests")
class DbTokenBlacklistServiceTest {

    @Mock
    TokenBlacklistMapper mapper;

    @Captor
    ArgumentCaptor<TokenBlacklist> entryCaptor;

    DbTokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new DbTokenBlacklistService(mapper);
    }

    @Test
    @DisplayName("blacklist: ttl<=0 时不入库")
    void blacklist_zeroOrNegativeTtl_doesNothing() {
        service.blacklist("token123", 0);
        service.blacklist("token123", -10);
        verify(mapper, never()).insert(any(TokenBlacklist.class));
    }

    @Test
    @DisplayName("blacklist: 计算 SHA256 并设置过期时间后入库")
    void blacklist_success() {
        service.blacklist("my-jwt-token", 3600);
        verify(mapper).insert(entryCaptor.capture());
        TokenBlacklist entry = entryCaptor.getValue();
        assertNotNull(entry.getTokenHash());
        assertEquals(64, entry.getTokenHash().length()); // SHA-256 hex is 64 chars
        assertNotNull(entry.getExpiresAt());
    }

    @Test
    @DisplayName("isBlacklisted: 查到未过期记录返回 true，否则 false")
    void isBlacklisted_checksDatabase() {
        when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertTrue(service.isBlacklisted("active-blacklisted-token"));

        when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        assertFalse(service.isBlacklisted("not-blacklisted-token"));
    }

    @Test
    @DisplayName("cleanExpiredTokens: 执行过期黑名单记录清理")
    void cleanExpiredTokens_success() {
        when(mapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);
        service.cleanExpiredTokens();
        verify(mapper).delete(any(LambdaQueryWrapper.class));
    }
}
