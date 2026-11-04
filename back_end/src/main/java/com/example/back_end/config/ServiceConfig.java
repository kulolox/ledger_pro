package com.example.back_end.config;

import com.example.back_end.mapper.TokenBlacklistMapper;
import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.service.IVerificationCodeService;
import com.example.back_end.service.impl.DbTokenBlacklistService;
import com.example.back_end.service.impl.MemoryVerificationCodeService;
import com.example.back_end.service.impl.RedisTokenBlacklistService;
import com.example.back_end.service.impl.RedisVerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Slf4j
public class ServiceConfig {

    // 两个同类型 Bean 互斥装配：有 Redis 用 Redis 黑名单，无则落库 token_blacklist 表；调用方只注入接口无感知
    @Bean
    @ConditionalOnExpression("!'${REDIS_HOST:}'.isEmpty()")
    public ITokenBlacklistService redisTokenBlacklistService(StringRedisTemplate redis) {
        log.info("黑名单服务使用 Redis 实现");
        return new RedisTokenBlacklistService(redis);
    }

    @Bean
    @ConditionalOnExpression("'${REDIS_HOST:}'.isEmpty()")
    public ITokenBlacklistService dbTokenBlacklistService(TokenBlacklistMapper mapper) {
        log.info("黑名单服务降级为 DB 实现");
        return new DbTokenBlacklistService(mapper);
    }

    @Bean
    @ConditionalOnExpression("!'${REDIS_HOST:}'.isEmpty()")
    public IVerificationCodeService redisVerificationCodeService(StringRedisTemplate redis) {
        log.info("验证码服务使用 Redis 实现");
        return new RedisVerificationCodeService(redis);
    }

    @Bean
    @ConditionalOnExpression("'${REDIS_HOST:}'.isEmpty()")
    public IVerificationCodeService memoryVerificationCodeService() {
        log.info("验证码服务降级为内存实现");
        return new MemoryVerificationCodeService();
    }
}
