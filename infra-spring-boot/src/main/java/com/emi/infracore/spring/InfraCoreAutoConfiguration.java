package com.emi.infracore.spring;

import com.emi.infracore.cache.CacheStore;
import com.emi.infracore.cache.RedisCacheStore;
import com.emi.infracore.factory.InfraCoreFactory;
import com.emi.infracore.idempotency.IdempotencyStore;
import com.emi.infracore.idempotency.RedisIdempotencyStore;
import com.emi.infracore.ratelimiter.RateLimiterStore;
import com.emi.infracore.ratelimiter.RedisRateLimiterStore;
import com.emi.infracore.stock.RedisStockStore;
import com.emi.infracore.stock.StockStore;
import com.emi.infracore.util.KeyBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Spring Boot auto-configuration for infra-core components.
 * 
 * This configuration:
 * - Creates RedisTemplate beans with proper serialization
 * - Sets up all infra-core store implementations
 * - Only activates if infra-core is on the classpath
 * - Can be overridden with custom beans
 * 
 * Usage: Just add infra-spring-boot dependency. Automatic!
 */
@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, InfraCoreFactory.class})
public class InfraCoreAutoConfiguration {

    /**
     * Create and configure RedisTemplate for object operations.
     * Delegates to Spring Boot's auto-configuration but adds our serialization.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> infraCoreRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Create StringRedisTemplate for string-based operations (like stock management).
     * Spring Boot auto-provides this, but we make it explicit.
     */
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate infraCoreStringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * Create KeyBuilder utility bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyBuilder keyBuilder() {
        return new KeyBuilder();
    }

    /**
     * Create RateLimiter store bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterStore rateLimiterStore(RedisTemplate<String, Object> infraCoreRedisTemplate) {
        return new RedisRateLimiterStore(infraCoreRedisTemplate);
    }

    /**
     * Create Idempotency store bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotencyStore idempotencyStore(KeyBuilder keyBuilder,
                                            RedisTemplate<String, Object> infraCoreRedisTemplate) {
        return new RedisIdempotencyStore(keyBuilder, infraCoreRedisTemplate);
    }

    /**
     * Create Cache store bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheStore cacheStore(RedisTemplate<String, Object> infraCoreRedisTemplate) {
        return new RedisCacheStore(infraCoreRedisTemplate);
    }

    /**
     * Create Stock store bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public StockStore stockStore(StringRedisTemplate infraCoreStringRedisTemplate,
                                KeyBuilder keyBuilder) {
        return new RedisStockStore(infraCoreStringRedisTemplate, keyBuilder);
    }

    /**
     * Create the factory bean for manual use if needed.
     */
    @Bean
    @ConditionalOnMissingBean
    public InfraCoreFactory infraCoreFactory(RedisTemplate<String, Object> infraCoreRedisTemplate,
                                            StringRedisTemplate infraCoreStringRedisTemplate) {
        return new InfraCoreFactory(infraCoreRedisTemplate, infraCoreStringRedisTemplate);
    }
}
