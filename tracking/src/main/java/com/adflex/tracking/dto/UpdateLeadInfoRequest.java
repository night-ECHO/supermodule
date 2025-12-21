package com.adflex.tracking.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateLeadInfoRequest {
    private String fullName;
    private String email;
    private String mbRefId;
    private String businessAddress;
    private List<String> businessNameOptions;
    private Long charterCapital;
    private String industryNeeds;
    private String assignedToOrg;
}
