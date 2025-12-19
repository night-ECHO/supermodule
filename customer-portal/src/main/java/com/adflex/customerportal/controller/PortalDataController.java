package com.adflex.customerportal.controller;

import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.repository.LeadProgressRepository;
import com.example.user_portal.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/data")
public class PortalDataController {

    @Autowired
    private LeadProgressRepository leadProgressRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/progress")
    public ResponseEntity<?> getMyProgress(@RequestHeader("Authorization") String tokenHeader) {
        // 1. Lấy LeadID từ Token (Để bảo mật, không cho khách truyền ID bừa bãi)
        String token = tokenHeader.substring(7); // Bỏ chữ "Bearer "
        String trackingToken = jwtUtil.extractUsername(token);

        // Lưu ý: Nếu hàm extractUsername trả về Token, bạn cần tìm LeadID từ Token đó trước.
        // Nhưng để nhanh, giả sử bạn lưu LeadID vào token hoặc FE gửi kèm.
        // CÁCH ĐƠN GIẢN NHẤT HIỆN TẠI (Cho FE gửi LeadID lên):
        return ResponseEntity.status(401).body("Vui lòng dùng API bên dưới có truyền LeadID");
    }

    // 👇 Dùng API này cho dễ (FE sẽ gửi LeadID đang lưu trong Session lên)
    @GetMapping("/progress/{leadId}")
    public ResponseEntity<?> getProgressByLeadId(@PathVariable String leadId) {
        // Lấy danh sách tiến độ, sắp xếp từ cũ đến mới
        List<LeadProgress> list = leadProgressRepository.findByLeadIdOrderByCreatedAtAsc(leadId);
        return ResponseEntity.ok(list);
    }
}