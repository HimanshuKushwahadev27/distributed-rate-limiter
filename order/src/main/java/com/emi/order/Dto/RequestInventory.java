package com.emi.order.Dto;

public record RequestInventory(
        Integer quantity,
        String skuCode
) {
}
