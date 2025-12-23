package com.adflex.tracking.controller;

import com.adflex.tracking.entity.Order;
import com.adflex.tracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ AdFlex Ops (giả sử role ADMIN)
public class OrderController {

    private final OrderService orderService;
    

    // Manual confirm payment
    @PostMapping("/{orderId}/confirm-payment")
    public ResponseEntity<Order> confirmPayment(
            @PathVariable String orderId,
            Principal principal // Lấy user hiện tại (AdFlex Ops)
    ) {
        String actor = principal != null ? principal.getName() : "system";
        Order updatedOrder = orderService.confirmPayment(orderId, actor);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{orderId}/public-payment")
    public ResponseEntity<Map<String, Object>> getOrderPublicPaymentInfo(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getPublicPaymentInfoByOrderId(orderId));
    }

    // @GetMapping("/public/pay/{token}")
    // public ResponseEntity<?> getPublicOrder(@PathVariable UUID token) {
    //     Order order = orderService.getPublicOrderByToken(token);
    //     return ResponseEntity.ok(orderService.generatePublicPaymentInfo(order));
    // }
    

        // Manual confirm contract (AdFlex only)
    @PostMapping("/{orderId}/confirm-contract")
    public ResponseEntity<Order> confirmContract(
            @PathVariable String orderId,
            Principal principal
    ) {
        String actor = principal != null ? principal.getName() : "system";
        Order updatedOrder = orderService.confirmContract(orderId, actor);
        return ResponseEntity.ok(updatedOrder);
    }

    // Upload scan hợp đồng bản cứng (PDF) - liên kết với order
    @PostMapping("/{orderId}/upload-contract")
    public ResponseEntity<?> uploadContract(
            @PathVariable String orderId,
            @RequestParam("file") MultipartFile file, // PDF only
            Principal principal
    ) {
        if (!file.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Chỉ chấp nhận file PDF");
        }
        Map<String, Object> resp = orderService.uploadContractScan(orderId, file, principal.getName());
        return ResponseEntity.ok(resp);
    }
    // Sẽ thêm confirm contract và upload ở phần sau
}
