package com.emi.order.controller;

import com.emi.order.Dto.RequestInventory;
import com.emi.order.Dto.ResponseInventory;
import com.emi.order.Dto.UpdateRequestDto;
import com.emi.order.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;


    @PostMapping("/create")
    public ResponseEntity<String> createInventory(
            @RequestBody RequestInventory request,
            @RequestHeader("Idempotency-Key") UUID requestId
    ){
        inventoryService.createInventory(request, requestId);
        return ResponseEntity.ok("Inventory created successfully");
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateInventory(
            @RequestBody UpdateRequestDto request,
            @RequestHeader("Idempotency-Key") UUID requestId
    ){
        inventoryService.updateInventory(request, requestId);
        return ResponseEntity.ok("Inventory updated successfully"); 
    }


    @GetMapping("/get")
    public ResponseEntity<List<ResponseInventory>> getInventory(
        @AuthenticationPrincipal Jwt jwt
    ){
        return ResponseEntity.ok(inventoryService.getInventory());
    }


}
