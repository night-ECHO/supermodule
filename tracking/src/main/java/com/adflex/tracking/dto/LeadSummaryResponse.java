package com.adflex.tracking.dto;

import com.adflex.profile.entity.LeadStatus;
import com.adflex.tracking.enums.PaymentStatus;
import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LeadSummaryResponse {
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

    private String packageCode;
    private Long packageAmount;
    private PaymentStatus paymentStatus;
    private String orderId;
}
