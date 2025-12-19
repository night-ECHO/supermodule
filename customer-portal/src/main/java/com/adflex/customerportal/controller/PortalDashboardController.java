package com.adflex.customerportal.controller;

import com.adflex.customerportal.service.PortalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/dashboard")
public class PortalDashboardController {

    @Autowired
    private PortalService portalService;

    @GetMapping("/{leadId}")
    public ResponseEntity<?> getDashboard(@PathVariable UUID leadId) {
        try {
            return ResponseEntity.ok(portalService.getDashboard(leadId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}