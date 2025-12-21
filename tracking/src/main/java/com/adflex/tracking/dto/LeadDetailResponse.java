package com.adflex.tracking.dto;

import com.adflex.profile.entity.LeadStatus;
import com.adflex.tracking.enums.PaymentStatus;
import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class LeadDetailResponse {
    private String id;
    private String trackingToken;
    private String fullName;
    private String phone;
    private String email;
    private String mbRefId;
    private LeadStatus status;
    private String assignedToOrg;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private Instant updatedAt;
    private Boolean isDuplicate;

    private String businessAddress;
    private List<String> businessNameOptions;
    private Long charterCapital;
    private String industryNeeds;

    private String packageCode;
    private Long packageAmount;
    private PaymentStatus paymentStatus;
    private String orderId;
}
