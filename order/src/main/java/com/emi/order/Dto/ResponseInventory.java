package com.emi.order.Dto;

import java.util.UUID;

public record ResponseInventory(
  UUID id,
  Integer quantity,
  String skuCode
) {
  
}
