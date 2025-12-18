package com.adflex.tracking.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCustomerPortalCredentialsResponse {
    private String leadId;
    private String trackingToken;
    private String accessCode;
    private String portalBaseUrl;
    private String path;
    private String link;
}
