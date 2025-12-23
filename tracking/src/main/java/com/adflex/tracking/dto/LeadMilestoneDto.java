package com.adflex.tracking.dto;

import com.adflex.tracking.enums.MilestoneStatus;
import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime startedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime completedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime updatedAt;

    private String proofDocId;
    private String fileLink;
    private String note;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime dueAt; // <-- thêm để SLA dùng

    // Danh sách proof đa file (UI dùng để hiển thị không bị ghi đè)
    private List<ProofDocumentDto> proofs;
}
