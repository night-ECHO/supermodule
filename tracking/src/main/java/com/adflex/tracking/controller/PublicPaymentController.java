package com.adflex.tracking.controller;

import com.adflex.tracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.adflex.tracking.entity.Order;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublicPaymentController {  // Không có @PreAuthorize, không có /admin

    private final OrderService orderService;

    @GetMapping("/pay/{token}")  // Path sạch, công khai
    public ResponseEntity<Map<String, Object>> getPublicPaymentInfo(@PathVariable UUID token) {
        Order order = orderService.getPublicOrderByToken(token);
        return ResponseEntity.ok(orderService.generatePublicPaymentInfo(order));
    }
}