package com.emi.infracore.idempotency;

import com.emi.infracore.util.KeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final KeyBuilder keyBuilder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isFirstRequest(String requestId) {

        String key = keyBuilder.idempotencyKey(requestId);
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "PROCESSED", 5, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(success);
    }
}
