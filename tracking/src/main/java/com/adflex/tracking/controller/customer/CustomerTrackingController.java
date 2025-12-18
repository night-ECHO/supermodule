package com.adflex.tracking.controller.customer;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.entity.Document;
import com.adflex.tracking.dto.customer.CustomerPresignedUrlResponse;
import com.adflex.tracking.dto.customer.CustomerTrackingResponse;
import com.adflex.tracking.security.CustomerJwtUtil;
import com.adflex.tracking.service.CustomerPortalService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
public class CustomerTrackingController {

    private final CustomerPortalService customerPortalService;
    private final LeadRepository leadRepository;
    private final CustomerJwtUtil customerJwtUtil;

    public CustomerTrackingController(CustomerPortalService customerPortalService, LeadRepository leadRepository, CustomerJwtUtil customerJwtUtil) {
        this.customerPortalService = customerPortalService;
        this.leadRepository = leadRepository;
        this.customerJwtUtil = customerJwtUtil;
    }

    @GetMapping("/tracking")
    public ResponseEntity<CustomerTrackingResponse> tracking(HttpServletRequest request) {
        Lead lead = requireLeadFromCustomerToken(request);
        return ResponseEntity.ok(customerPortalService.buildTrackingResponse(lead));
    }

    @GetMapping("/documents/{docId}")
    public ResponseEntity<CustomerPresignedUrlResponse> getDocumentUrl(
            @PathVariable("docId") String docId,
            HttpServletRequest request
    ) {
        Lead lead = requireLeadFromCustomerToken(request);
        UUID documentId = UUID.fromString(docId);
        customerPortalService.requirePublicDocumentForLead(lead.getId(), documentId);
        return ResponseEntity.ok(CustomerPresignedUrlResponse.builder()
                .url("/api/customer/documents/" + docId + "/download")
                .build());
    }

    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable("docId") String docId,
            HttpServletRequest request
    ) throws Exception {
        Lead lead = requireLeadFromCustomerToken(request);
        UUID documentId = UUID.fromString(docId);
        Document document = customerPortalService.requirePublicDocumentForLead(lead.getId(), documentId);

        Path file = Path.of(document.getStorageKey());
        if (!Files.exists(file)) {
            throw new RuntimeException("File not found");
        }

        String filename = StringUtils.hasText(document.getName()) ? document.getName() : file.getFileName().toString();
        String contentType = Files.probeContentType(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(file))
                .body(new FileSystemResource(file));
    }

    private Lead requireLeadFromCustomerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization");
        }
        String token = auth.substring(7);
        if (!customerJwtUtil.isCustomerToken(token)) {
            throw new RuntimeException("Invalid customer token");
        }
        UUID leadId = customerJwtUtil.extractLeadId(token);
        UUID trackingToken = customerJwtUtil.extractTrackingToken(token);

        Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new RuntimeException("Lead not found"));
        if (lead.getTrackingToken() == null || !trackingToken.equals(lead.getTrackingToken())) {
            throw new RuntimeException("Invalid tracking token");
        }
        return lead;
    }
}
