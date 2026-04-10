package com.emi.order.controller;

import com.emi.order.Dto.RequestOrder;
import com.emi.order.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody RequestOrder requestOrder,
    @RequestHeader("Idempotenct-key") UUID requestId
    ){
        orderService.createOrder(requestOrder, requestId);
        return ResponseEntity.ok("Order created successfully");
    }
}
