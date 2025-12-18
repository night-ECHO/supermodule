package com.adflex.tracking.controller.admin;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.dto.admin.AdminCustomerPortalCredentialsResponse;
import com.adflex.tracking.service.CustomerCredentialsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Admin-only helper endpoints to support manual customer portal delivery
 * when Zalo ZNS is not integrated yet.
 */
@RestController
@RequestMapping("/api/admin/leads")
public class AdminCustomerPortalController {

    private final LeadRepository leadRepository;
    private final CustomerCredentialsService customerCredentialsService;
    private final String portalBaseUrl;

    public AdminCustomerPortalController(
            LeadRepository leadRepository,
            CustomerCredentialsService customerCredentialsService,
            @Value("${customer.portal.base-url:https://portal.adflex.vn/track}") String portalBaseUrl
    ) {
        this.leadRepository = leadRepository;
        this.customerCredentialsService = customerCredentialsService;
        this.portalBaseUrl = portalBaseUrl;
    }

    /**
     * Generates credentials if missing and returns a usable link + passcode for manual sending.
     * If credentials already exist, accessCode will be null (because it's stored as BCrypt hash).
     */
    @PostMapping("/{leadId}/customer-portal/ensure")
    public ResponseEntity<AdminCustomerPortalCredentialsResponse> ensure(
            @PathVariable("leadId") String leadId,
            @RequestParam(value = "portal_base_url", required = false) String portalBaseUrlOverride
    ) {
        Lead lead = leadRepository.findById(UUID.fromString(leadId))
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        String plain = customerCredentialsService.ensureCredentials(lead);
        return ResponseEntity.ok(toResponse(lead, plain, portalBaseUrlOverride));
    }

    /**
     * Resets passcode (generates a new one) and returns the plain value for manual sending.
     */
    @PostMapping("/{leadId}/customer-portal/reset-passcode")
    public ResponseEntity<AdminCustomerPortalCredentialsResponse> resetPasscode(
            @PathVariable("leadId") String leadId,
            @RequestParam(value = "portal_base_url", required = false) String portalBaseUrlOverride
    ) {
        Lead lead = leadRepository.findById(UUID.fromString(leadId))
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        String plain = customerCredentialsService.resetAccessCode(lead);
        return ResponseEntity.ok(toResponse(lead, plain, portalBaseUrlOverride));
    }

    private AdminCustomerPortalCredentialsResponse toResponse(Lead lead, String accessCodePlain, String portalBaseUrlOverride) {
        String baseUrl = (portalBaseUrlOverride != null && !portalBaseUrlOverride.isBlank())
                ? portalBaseUrlOverride
                : portalBaseUrl;
        String token = lead.getTrackingToken() != null ? lead.getTrackingToken().toString() : null;
        String path = token != null ? "/track/" + token : null;
        String link = token != null ? buildLink(baseUrl, token) : null;
        return AdminCustomerPortalCredentialsResponse.builder()
                .leadId(lead.getId().toString())
                .trackingToken(token)
                .accessCode(accessCodePlain)
                .portalBaseUrl(baseUrl)
                .path(path)
                .link(link)
                .build();
    }

    private static String buildLink(String baseUrl, String token) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (trimmed.endsWith("/track")) {
            return trimmed + "/" + token;
        }
        return trimmed + "/track/" + token;
    }
}
