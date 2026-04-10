package com.emi.order.repository;

import com.emi.order.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    boolean existsBySkuCode(String skuCode);
    Optional<Inventory> findBySkuCode(String skuCode);
}
