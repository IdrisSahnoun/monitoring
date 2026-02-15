package com.vehicle.diagnostic.monitoring.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis and caching configuration
 */
@Configuration
@EnableCaching
public class RedisConfig {
    
    /**
     * Configure RedisTemplate for manual Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serialization for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serialization for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * Configure Cache Manager with different TTL for different caches
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // Custom TTL for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Session cache - 10 minutes
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Worker events cache - 10 minutes
        cacheConfigurations.put("workerEvents", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Performance metrics cache - 5 minutes
        cacheConfigurations.put("performanceMetrics", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Dashboard stats cache - 5 minutes
        cacheConfigurations.put("dashboardStats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Realtime stats cache - 1 minute (shorter TTL for real-time data)
        cacheConfigurations.put("realtimeStats", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // Worker stats cache - 15 minutes
        cacheConfigurations.put("workerStats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
