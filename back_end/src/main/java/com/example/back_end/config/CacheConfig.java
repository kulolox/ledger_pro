package com.example.back_end.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
@Slf4j
public class CacheConfig {

    @Bean
    @ConditionalOnExpression("!'${REDIS_HOST:}'.isEmpty()")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 有 Redis：30 分钟 TTL，JacksonSerializer 白名单反序列化
        log.info("缓存使用 Redis 实现");

        ObjectMapper mapper = new ObjectMapper();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonSerializer(mapper)));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean 
    @ConditionalOnExpression("'${REDIS_HOST:}'.isEmpty()")
    public CacheManager inMemoryCacheManager() {
        // 无 Redis：进程内内存缓存（@Cacheable 强依赖 CacheManager，无它应用起不来）
        log.info("缓存降级为进程内内存实现");
        return new ConcurrentMapCacheManager("transactions");
    }
}
