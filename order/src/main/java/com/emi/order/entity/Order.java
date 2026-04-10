package com.emi.order.entity;

import java.math.BigDecimal;
import java.util.UUID;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@Table(name="orders")
public class Order {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private UUID id;

    private BigDecimal pricePaid;

    private Integer quantity;

    private String skuCode;

}