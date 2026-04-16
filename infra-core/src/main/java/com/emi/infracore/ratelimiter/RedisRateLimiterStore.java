package com.emi.infracore.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class RedisRateLimiterStore implements RateLimiterStore{

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public long incrementRequestCount(String key, long ttlSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }

        return count != null ? count : 0;
    }
}
