package com.adflex.customerportal.controller;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.example.user_portal.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer/auth") // Url chuẩn
public class PortalAuthController {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String trackingToken = request.get("trackingToken");
        String accessCode = request.get("accessCode");

        // 1. Kiểm tra thông tin trong DB
        Optional<Lead> leadOpt = leadRepository.findByTrackingTokenAndAccessCode(trackingToken, accessCode);

        if (leadOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Thông tin truy cập không chính xác");
        }

        Lead lead = leadOpt.get();

        // 2. Tạo Token (Dùng hàm mới mà tôi vừa sửa cho ông ở JwtUtil cũ)
        String token = jwtUtil.generateToken(lead.getTrackingToken(), "CUSTOMER");

        // 3. Trả về kết quả
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("leadId", lead.getId());
        response.put("customerName", lead.getFullName());

        return ResponseEntity.ok(response);
    }
}