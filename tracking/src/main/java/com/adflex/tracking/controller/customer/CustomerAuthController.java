package com.adflex.tracking.controller.customer;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.dto.customer.CustomerAuthRequest;
import com.adflex.tracking.dto.customer.CustomerAuthResponse;
import com.adflex.tracking.service.CustomerRateLimitService;
import com.adflex.tracking.security.CustomerJwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
public class CustomerAuthController {

    private final LeadRepository leadRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRateLimitService rateLimitService;
    private final CustomerJwtUtil customerJwtUtil;

    public CustomerAuthController(
            LeadRepository leadRepository,
            PasswordEncoder passwordEncoder,
            CustomerRateLimitService rateLimitService,
            CustomerJwtUtil customerJwtUtil
    ) {
        this.leadRepository = leadRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimitService = rateLimitService;
        this.customerJwtUtil = customerJwtUtil;
    }

    @PostMapping("/auth/{token}")
    public ResponseEntity<?> auth(
            @PathVariable("token") String token,
            @RequestBody CustomerAuthRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String accessCode = request != null ? request.getAccessCode() : null;
        if (accessCode == null || accessCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "access_code is required"));
        }

        UUID trackingToken;
        try {
            trackingToken = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Invalid token"));
        }

        String key = trackingToken + ":" + clientIp(httpServletRequest);
        var locked = rateLimitService.checkLocked(key);
        if (!locked.allowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "message", "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau",
                            "locked_until", locked.lockedUntil() != null ? locked.lockedUntil().toString() : null
                    ));
        }

        Lead lead = leadRepository.findByTrackingToken(trackingToken).orElse(null);
        if (lead == null || lead.getAccessCode() == null || lead.getAccessCode().isBlank()) {
            // do not reveal whether token exists
            rateLimitService.onFailedAttempt(key);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Sai passcode"));
        }

        boolean ok = passwordEncoder.matches(accessCode, lead.getAccessCode());
        if (!ok) {
            var decision = rateLimitService.onFailedAttempt(key);
            if (!decision.allowed()) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of(
                                "message", "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau",
                                "locked_until", decision.lockedUntil() != null ? decision.lockedUntil().toString() : null
                        ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Sai passcode"));
        }

        rateLimitService.clear(key);
        String jwt = customerJwtUtil.generate(lead.getId(), trackingToken);
        return ResponseEntity.ok(CustomerAuthResponse.builder()
                .token(jwt)
                .expiresInMs(customerJwtUtil.getExpirationMs())
                .build());
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
