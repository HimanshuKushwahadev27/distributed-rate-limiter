package com.emi.infracore.cache;

import java.util.function.Supplier;

public interface CacheStore {

    void put(String key, Object value, long ttlSeconds);
    Object get(String key);
    void evict(String key);

    public <T> T getOrLoad(String key, Supplier<T> dbCall, long ttlSeconds);
}
