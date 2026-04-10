package com.emi.order.Dto;

import java.util.UUID;

public record UpdateRequestDto(
        UUID id,
        String skuCode,
        Integer quantity
) {
}
