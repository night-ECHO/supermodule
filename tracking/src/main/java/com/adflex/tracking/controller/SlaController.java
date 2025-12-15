package com.adflex.tracking.controller;

import com.adflex.tracking.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sla")
@RequiredArgsConstructor
public class SlaController {

    private final SlaService slaService;

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdue() {
        return ResponseEntity.ok(slaService.getOverdueSteps());
    }
}
