package com.emi.infracore.ratelimiter;

public interface RateLimiterStore {
    long incrementRequestCount(String key, long ttlSeconds);
}
