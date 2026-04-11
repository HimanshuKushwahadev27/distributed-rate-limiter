package com.emi.order.service;

import com.emi.infracore.stock.StockStore;
import com.emi.infracore.idempotency.IdempotencyStore;
import com.emi.order.Dto.RequestOrder;
import com.emi.order.entity.Inventory;
import com.emi.order.entity.Order;
import com.emi.order.repository.InventoryRepository;
import com.emi.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService{

    private final OrderRepository orderRepository;
    private final StockStore stockStore;
    private final IdempotencyStore idempotencyStore;
    private final InventoryRepository inventoryRepository;

    public void createOrder(RequestOrder requestOrder, UUID requestId){
        
        if(!idempotencyStore.isFirstRequest(requestId.toString())){
            log.info("Duplicate request: {}", requestId);
            return;
        }

        boolean success = stockStore.reduceStock(

                requestOrder.skuCode(),
                requestOrder.quantity()
        );

        if (!success) {
            throw new RuntimeException("Not enough stock");
        }
        
        Inventory inventory  = inventoryRepository.findBySkuCode(requestOrder.skuCode())
                .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + requestOrder.skuCode()));

        inventory.setQuantity(inventory.getQuantity() - requestOrder.quantity());

        inventoryRepository.save(inventory);

        Order order = new Order();
        order.setQuantity(requestOrder.quantity());
        order.setSkuCode(requestOrder.skuCode());
        order.setPricePaid(requestOrder.pricePaid());

        log.info("Updated the database");
        orderRepository.save(order);
    }
}
