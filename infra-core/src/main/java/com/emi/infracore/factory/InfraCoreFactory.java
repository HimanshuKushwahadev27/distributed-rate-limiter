package com.emi.infracore.factory;

import com.emi.infracore.cache.CacheStore;
import com.emi.infracore.cache.RedisCacheStore;
import com.emi.infracore.idempotency.IdempotencyStore;
import com.emi.infracore.idempotency.RedisIdempotencyStore;
import com.emi.infracore.ratelimiter.RateLimiterStore;
import com.emi.infracore.ratelimiter.RedisRateLimiterStore;
import com.emi.infracore.stock.RedisStockStore;
import com.emi.infracore.stock.StockStore;
import com.emi.infracore.util.KeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Factory class for creating infracore components.
 * Framework-agnostic factory for instantiating all infracore services.
 * 
 * Usage:
 * <pre>
 * RedisTemplate<String, Object> redisTemplate = ...; // from your framework
 * StringRedisTemplate stringRedisTemplate = ...;
 * InfraCoreFactory factory = new InfraCoreFactory(redisTemplate, stringRedisTemplate);
 * 
 * RateLimiterStore store = factory.createRateLimiterStore();
 * IdempotencyStore idempotency = factory.createIdempotencyStore();
 * </pre>
 */
public class InfraCoreFactory {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final KeyBuilder keyBuilder;

    /**
     * Create a new InfraCoreFactory.
     *
     * @param redisTemplate Redis template for object operations
     * @param stringRedisTemplate Redis template for string operations
     */
    public InfraCoreFactory(RedisTemplate<String, Object> redisTemplate,
                          StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.keyBuilder = new KeyBuilder();
    }

    public RateLimiterStore createRateLimiterStore() {
        return new RedisRateLimiterStore(redisTemplate);
    }

    public IdempotencyStore createIdempotencyStore() {
        return new RedisIdempotencyStore(keyBuilder, redisTemplate);
    }

    public CacheStore createCacheStore() {
        return new RedisCacheStore(redisTemplate);
    }

    public StockStore createStockStore() {
        return new RedisStockStore(stringRedisTemplate, keyBuilder);
    }

    public KeyBuilder getKeyBuilder() {
        return keyBuilder;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }
}
