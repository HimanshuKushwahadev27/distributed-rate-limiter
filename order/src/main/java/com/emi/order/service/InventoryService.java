package com.emi.order.service;

import com.emi.infracore.util.KeyBuilder;
import com.emi.order.Dto.RequestInventory;
import com.emi.order.Dto.ResponseInventory;
import com.emi.order.Dto.UpdateRequestDto;
import com.emi.order.entity.Inventory;
import com.emi.order.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.emi.infracore.idempotency.IdempotencyStore;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final KeyBuilder keyBuilder;
    private final InventoryRepository inventoryRepository;
    private final StringRedisTemplate redisTemplate;
    private final IdempotencyStore  idempotencyStore;



    public void createInventory(RequestInventory request, UUID requestId){
        
        if(!idempotencyStore.isFirstRequest(requestId.toString())){
          log.info("Duplicate request: {}", requestId);
          return;
        }

        if(inventoryRepository.existsBySkuCode(request.skuCode())){
            throw new RuntimeException("Inventory already exists for SKU: " + request.skuCode());
        }

        Inventory inventory = new Inventory();
        inventory.setQuantity(request.quantity());
        inventory.setSkuCode(request.skuCode());
        inventoryRepository.save(inventory);

        redisTemplate.opsForValue().set(
                keyBuilder.stockKey(request.skuCode()),
                String.valueOf(request.quantity())
        );

    }

    public void updateInventory(UpdateRequestDto request, UUID requestId
    ){
        
        if(!idempotencyStore.isFirstRequest(requestId.toString())){
            log.info("Duplicate request: {}", requestId);
            throw new RuntimeException("Duplicate request");
        }


        Inventory inventory = inventoryRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setQuantity(request.quantity());

        inventoryRepository.save(inventory);

        redisTemplate.opsForValue().set(
                keyBuilder.stockKey(request.skuCode()),
                String.valueOf(request.quantity())
        );
    }

    public List<ResponseInventory> getInventory(){

        

        return inventoryRepository.findAll().stream().map(inventory -> new ResponseInventory(
                inventory.getId(),
                inventory.getQuantity(),
                inventory.getSkuCode()
        )).collect(Collectors.toList());
    }

}

