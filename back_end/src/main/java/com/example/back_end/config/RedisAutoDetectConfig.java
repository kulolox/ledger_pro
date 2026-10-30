package com.example.back_end.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

// 设置 REDIS_HOST 环境变量才装配 Redis Bean；未设置则整个 Redis 栈缺失，依赖 @ConditionalOnExpression 的降级实现生效
@Configuration
@ConditionalOnExpression("!'${REDIS_HOST:}'.isEmpty()")
public class RedisAutoDetectConfig {

    @Value("${REDIS_HOST}")
    private String host;

    @Value("${REDIS_PORT:6379}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
