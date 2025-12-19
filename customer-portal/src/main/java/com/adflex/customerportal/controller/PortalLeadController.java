package com.adflex.customerportal.controller;

import com.adflex.tracking.dto.LeadMilestoneDto;
import com.adflex.tracking.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Để Frontend gọi không bị lỗi
public class PortalLeadController {

    private final ProgressService progressService;

    // API: GET /api/portal/leads/{id}/progress
    @GetMapping("/{leadId}/progress")
    public ResponseEntity<List<LeadMilestoneDto>> getProgress(@PathVariable String leadId) {
        // Gọi Service lấy danh sách tiến độ
        return ResponseEntity.ok(progressService.getLeadMilestones(leadId));
    }
}