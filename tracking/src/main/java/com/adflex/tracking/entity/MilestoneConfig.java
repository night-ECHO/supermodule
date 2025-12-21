package com.adflex.tracking.entity;

import com.adflex.tracking.config.DateTimeConstants;
import com.adflex.tracking.enums.MilestoneType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestone_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneConfig {

    @Id
    @Column(length = 50)
    private String code; 

    private String name;

    @Enumerated(EnumType.STRING)
    private MilestoneType type;

    @Column(name = "min_package_level")
    private Integer minPackageLevel;

    @Column(name = "sequence_order")
    private Integer sequenceOrder; 

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "required_proof")
    private Boolean requiredProof;

    @Column(name = "payment_required")
    private Boolean paymentRequired;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
