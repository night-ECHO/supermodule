package com.adflex.tracking.dto;

import com.adflex.tracking.enums.MilestoneStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadMilestoneDto {
    private String leadId;
    private String milestoneCode;
    private String milestoneName;
    private String milestoneType;
    private MilestoneStatus status;
    private Integer sequenceOrder;
    private Boolean requiredProof;
    private Boolean paymentRequired;
    private Integer slaHours;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String proofDocId;
    private String fileLink;
    private String note;

    private LocalDateTime dueAt; // <-- thêm để SLA dùng
}
