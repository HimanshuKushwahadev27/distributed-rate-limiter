package com.emi.infracore.stock;

public interface StockStore {
    boolean reduceStock(String productId, int quantity);
}
