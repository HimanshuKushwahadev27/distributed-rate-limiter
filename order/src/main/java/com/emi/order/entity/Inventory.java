package com.emi.order.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@Table(name="inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private UUID id;

    private Integer quantity;

    private String skuCode;
}
