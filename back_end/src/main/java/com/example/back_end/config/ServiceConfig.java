package com.example.back_end.config;

import com.example.back_end.mapper.TokenBlacklistMapper;
import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.service.impl.DbTokenBlacklistService;
import com.example.back_end.service.impl.RedisTokenBlacklistService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class ServiceConfig {

    @Bean
    @ConditionalOnExpression("!'${REDIS_HOST:}'.isEmpty()")
    public ITokenBlacklistService redisTokenBlacklistService(StringRedisTemplate redis) {
        return new RedisTokenBlacklistService(redis);
    }

    @Bean
    @ConditionalOnExpression("'${REDIS_HOST:}'.isEmpty()")
    public ITokenBlacklistService dbTokenBlacklistService(TokenBlacklistMapper mapper) {
        return new DbTokenBlacklistService(mapper);
    }
}
