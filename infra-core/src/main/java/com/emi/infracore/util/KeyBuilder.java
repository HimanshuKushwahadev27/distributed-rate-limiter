package com.emi.infracore.util;

import org.springframework.stereotype.Component;

@Component
public class KeyBuilder {
    private static final String SEPARATOR = ":" ;


    public  String rateLimitKey(String userId, String api, String window) {
        return String.join(SEPARATOR, "rate_limit", userId, api, window);
    }

    // ---------------- STOCK ----------------

    public   String stockKey(String productId) {
        return String.join(SEPARATOR, "stock", productId);
    }

    // ---------------- CACHE ----------------

    public  String productCacheKey(String productId) {
        return String.join(SEPARATOR, "cache", "product", productId);
    }

    // ---------------- IDEMPOTENCY ----------------

    public  String idempotencyKey(String requestId) {
        return String.join(SEPARATOR, "idempotency", requestId);
    }

    // ---------------- LOCK ----------------

    public  String lockKey(String resource) {
        return String.join(SEPARATOR, "lock", resource);
    }
}
