package com.emi.order.Dto;

import java.math.BigDecimal;

public record RequestOrder(
        BigDecimal pricePaid,
        Integer quantity,
        String skuCode
) {
}
