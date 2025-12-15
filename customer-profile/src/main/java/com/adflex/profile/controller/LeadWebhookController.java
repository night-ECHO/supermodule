package com.adflex.profile.controller;

import com.adflex.profile.dto.request.WebhookRequest;
import com.adflex.profile.dto.response.LeadResponse;
import com.adflex.profile.entity.Lead;
import com.adflex.profile.security.SpamGuard; // <--- 1. NHỚ IMPORT CÁI NÀY
import com.adflex.profile.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class LeadWebhookController {

    private final LeadService leadService;

    @Value("${app.api-key}")
    private String apiKey;


    @SpamGuard(maxRequests = 3)
    @PostMapping("/google-form")
    public ResponseEntity<?> receiveLead(
            @RequestHeader(value = "X-Api-Key", required = false) String headerKey,
            @Valid @RequestBody WebhookRequest body
    ) {
        // FR 1.1: Xác thực API Key
        if (headerKey == null || !headerKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        if (body == null || body.getData() == null) {
            return ResponseEntity.badRequest().body("Missing data");
        }

        Lead lead = leadService.processIncomingLead(body.getData());

        LeadResponse res = new LeadResponse(lead.getId(), lead.getStatus());
        HttpStatus status = Boolean.TRUE.equals(lead.getIsDuplicate())
                ? HttpStatus.OK        // lead trùng
                : HttpStatus.CREATED;  // lead mới

        return ResponseEntity.status(status).body(res);
    }
}