package com.emi.infracore.idempotency;

public interface IdempotencyStore {
    boolean isFirstRequest(String requestId);
}
