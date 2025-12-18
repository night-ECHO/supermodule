package com.adflex.tracking.dto;

import com.adflex.profile.entity.LeadStatus;
import com.adflex.tracking.enums.PaymentStatus;
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
    private Instant createdAt;
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
