package com.adflex.tracking.service;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.dto.LeadMilestoneDto;
import com.adflex.tracking.dto.customer.CustomerDocumentDto;
import com.adflex.tracking.dto.customer.CustomerTimelineItemDto;
import com.adflex.tracking.dto.customer.CustomerTrackingResponse;
import com.adflex.tracking.entity.Document;
import com.adflex.tracking.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomerPortalService {

    private final ProgressService progressService;
    private final DocumentRepository documentRepository;
    private final LeadRepository leadRepository;

    public CustomerPortalService(ProgressService progressService, DocumentRepository documentRepository, LeadRepository leadRepository) {
        this.progressService = progressService;
        this.documentRepository = documentRepository;
        this.leadRepository = leadRepository;
    }

    public Lead requireLeadById(UUID leadId) {
        return leadRepository.findById(leadId).orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    public CustomerTrackingResponse buildTrackingResponse(Lead lead) {
        List<LeadMilestoneDto> milestones = progressService.getLeadMilestones(lead.getId().toString()).stream()
                .filter(m -> "CORE".equalsIgnoreCase(m.getMilestoneType()))
                .toList();

        List<CustomerTimelineItemDto> timeline = milestones.stream()
                .map(m -> CustomerTimelineItemDto.builder()
                        .code(m.getMilestoneCode())
                        .name(m.getMilestoneName())
                        .milestoneType(m.getMilestoneType())
                        .status(m.getStatus() != null ? m.getStatus().name() : null)
                        .date(toInstant(m.getCompletedAt() != null ? m.getCompletedAt() : m.getStartedAt()))
                        .note(m.getNote())
                        .build())
                .toList();

        String currentStatus;
        if (milestones.stream().anyMatch(m -> m.getStatus() != null && "WAITING_PAYMENT".equals(m.getStatus().name()))) {
            currentStatus = "WAITING_PAYMENT";
        } else if (milestones.stream().anyMatch(m -> m.getStatus() != null && "IN_PROGRESS".equals(m.getStatus().name()))) {
            currentStatus = "IN_PROGRESS";
        } else if (!milestones.isEmpty() && milestones.stream().allMatch(m -> m.getStatus() != null && "COMPLETED".equals(m.getStatus().name()))) {
            currentStatus = "COMPLETED";
        } else {
            currentStatus = "IN_PROGRESS";
        }

        List<CustomerDocumentDto> documents = documentRepository.findByLeadIdAndIsPublicTrueOrderByUploadedAtDesc(lead.getId()).stream()
                .map(d -> CustomerDocumentDto.builder()
                        .id(d.getId().toString())
                        .name(d.getName())
                        .type(d.getType())
                        .date(d.getUploadedAt())
                        .milestoneCode(d.getMilestoneCode())
                        .build())
                .toList();

        Map<String, Object> leadInfo = new HashMap<>();
        leadInfo.put("company_name", lead.getBusinessNameOptions() != null && !lead.getBusinessNameOptions().isEmpty()
                ? lead.getBusinessNameOptions().get(0)
                : (lead.getFullName() != null ? lead.getFullName() : ""));
        // Use a mutable map to allow nullable optional fields without throwing (Map.of rejects nulls)
        leadInfo.put("mst", lead.getMbRefId()); // placeholder until MST is explicitly modeled

        return CustomerTrackingResponse.builder()
                .leadInfo(leadInfo)
                .currentStatus(currentStatus)
                .timeline(timeline)
                .documents(documents)
                .build();
    }

    public Document requirePublicDocumentForLead(UUID leadId, UUID docId) {
        return documentRepository.findByIdAndLeadId(docId, leadId)
                .filter(d -> Boolean.TRUE.equals(d.getIsPublic()))
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    private static Instant toInstant(java.time.LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(java.time.ZoneId.systemDefault()).toInstant();
    }
}
