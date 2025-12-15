package com.adflex.tracking.controller;

import com.adflex.profile.dto.request.LeadPayload;
import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.profile.service.LeadService;
import com.adflex.tracking.dto.LeadDetailResponse;
import com.adflex.tracking.dto.LeadSummaryResponse;
import com.adflex.tracking.entity.Order;
import com.adflex.tracking.repository.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/leads")
@RequiredArgsConstructor
public class LeadAdminController {

    private final LeadRepository leadRepository;
    private final OrderRepository orderRepository;
    private final LeadService leadService;

    @GetMapping
    public ResponseEntity<List<LeadSummaryResponse>> list(
            @RequestParam(value = "q", required = false) String keyword
    ) {
        List<Lead> leads;
        if (keyword != null && !keyword.isBlank()) {
            leads = leadRepository.findByFullNameContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrderByCreatedAtDesc(
                    keyword, keyword
            );
        } else {
            leads = leadRepository.findAllByOrderByCreatedAtDesc();
        }

        List<LeadSummaryResponse> payload = leads.stream()
                .map(this::toSummary)
                .toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadDetailResponse> getOne(@PathVariable("leadId") String leadId) {
        Lead lead = leadRepository.findById(UUID.fromString(leadId))
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return ResponseEntity.ok(toDetail(lead));
    }

    @PostMapping
    public ResponseEntity<LeadDetailResponse> create(@Valid @RequestBody LeadPayload payload) {
        Lead lead = leadService.processIncomingLead(payload);
        HttpStatus status = Boolean.TRUE.equals(lead.getIsDuplicate()) ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(toDetail(lead));
    }

    private LeadSummaryResponse toSummary(Lead lead) {
        Order order = getLatestOrder(lead.getId().toString());
        return LeadSummaryResponse.builder()
                .id(lead.getId().toString())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .mbRefId(lead.getMbRefId())
                .status(lead.getStatus())
                .assignedToOrg(lead.getAssignedToOrg())         
                .createdAt(lead.getCreatedAt())
                .packageCode(order != null ? order.getPackageCode() : null)
                .packageAmount(order != null ? order.getAmount() : null)
                .paymentStatus(order != null ? order.getPaymentStatus() : null)
                .orderId(order != null ? order.getId() : null)
                .build();
    }

    private LeadDetailResponse toDetail(Lead lead) {
        Order order = getLatestOrder(lead.getId().toString());
        return LeadDetailResponse.builder()
                .id(lead.getId().toString())
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .mbRefId(lead.getMbRefId())
                .status(lead.getStatus())
                .assignedToOrg(lead.getAssignedToOrg())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .isDuplicate(lead.getIsDuplicate())
                .businessAddress(lead.getBusinessAddress())
                .businessNameOptions(lead.getBusinessNameOptions())
                .charterCapital(lead.getCharterCapital())
                .industryNeeds(lead.getIndustryNeeds())
                .packageCode(order != null ? order.getPackageCode() : null)
                .packageAmount(order != null ? order.getAmount() : null)
                .paymentStatus(order != null ? order.getPaymentStatus() : null)
                .orderId(order != null ? order.getId() : null)
                .build();
    }

    private Order getLatestOrder(String leadId) {
        List<Order> orders = orderRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
        return (orders != null && !orders.isEmpty()) ? orders.get(0) : null;
    }
}
