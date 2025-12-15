package com.adflex.tracking.controller;

import com.adflex.tracking.dto.PaymentCallbackRequest;
import com.adflex.tracking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<?> pay(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.pay(orderId));
    }

    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.ok(paymentService.callback(request));
    }
}
