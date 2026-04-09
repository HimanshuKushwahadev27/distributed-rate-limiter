package com.emi.infracore.cache;

import com.emi.infracore.util.KeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RedisCacheStore implements  CacheStore{

    private final RedisTemplate<String, Object> redisTemplate;
    private final KeyBuilder keyBuilder;

    @Override
    public void put(String key, Object value, long ttlSeconds) {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> dbCall, long ttlSeconds) {
        Object cached = this.get(key);

        if(cached != null){
            return  (T)cached;
        }

        T value = dbCall.get();
        if(value!=null){
            this.put(key, value, ttlSeconds);
        }
        return value;
    }
}
