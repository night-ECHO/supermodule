package com.adflex.tracking.entity;

import com.adflex.tracking.enums.MilestoneStatus;
import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lead_id", "milestone_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "lead_id", nullable = false)
    private String leadId;

    @Column(name = "milestone_code", nullable = false)
    private String milestoneCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime startedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime completedAt;

    private String proofDocId;

    @Column(name = "file_link")
    private String fileLink;

    @Column(columnDefinition = "TEXT")
    private String note;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
