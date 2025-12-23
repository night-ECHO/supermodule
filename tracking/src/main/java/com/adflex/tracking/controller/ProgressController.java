package com.adflex.tracking.controller;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.dto.LeadDetailResponse;
import com.adflex.tracking.dto.UpdateLeadInfoRequest;
import com.adflex.tracking.entity.Order;
import com.adflex.tracking.repository.OrderRepository;
import com.adflex.tracking.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final LeadRepository leadRepository;
    private final OrderRepository orderRepository;

    // Giai đoạn 1 — Khởi tạo STEP_CONSULT cho lead mới
    @PostMapping("/{leadId}/init-progress")
    public ResponseEntity<?> initProgress(
            @PathVariable("leadId") String leadId
    ) {
        return ResponseEntity.ok(
                progressService.onLeadCreated(leadId)
        );
    }

     // Giai đoạn 2 — Xác nhận package hoặc chỉ tạo Addon
     
    @PostMapping("/{leadId}/confirm-package")
    public ResponseEntity<?> confirmPackage(
            @PathVariable("leadId") String leadId,
            @RequestBody Map<String, Object> body
    ) {

        // Lấy package_code (KHÔNG BẮT BUỘC)
        String packageCode = body.get("package_code") != null
                ? body.get("package_code").toString()
                : null;

        // Lấy addons nếu có
        List<String> addons = null;
        if (body.containsKey("addons") && body.get("addons") instanceof List<?> raw) {
            addons = raw.stream().map(Object::toString).toList();
        }

        // Lấy trạng thái thanh toán (chỉ dùng khi có package)
        boolean isPaid = body.get("is_paid") != null
                && Boolean.parseBoolean(body.get("is_paid").toString());

        // CASE A: GỬI ADDON ONLY (không có gói)
        if (packageCode == null || packageCode.isBlank()) {
            return ResponseEntity.ok(
                    progressService.confirmPackage(
                            leadId,
                            null,      
                            addons,
                            false       
                    )
            );
        }

        // CASE B: GÓI 1 / GÓI 2
        return ResponseEntity.ok(
                progressService.confirmPackage(
                        leadId,
                        packageCode,
                        addons,
                        isPaid
                )
        );
    }

    
     // Giai đoạn 2 — Update milestone: START / COMPLETE / FAIL
     
    @PostMapping("/{leadId}/progress/{milestoneCode}")
    public ResponseEntity<?> updateProgress(
            @PathVariable("leadId") String leadId,
            @PathVariable("milestoneCode") String milestoneCode,
            @RequestBody Map<String, Object> body
    ) {

        String action = (String) body.get("action");
        String proofDocId = (String) body.get("proof_doc_id");
        String proofFileLink = (String) body.get("proof_file_link");
        String note = (String) body.get("note");

        return ResponseEntity.ok(
                progressService.updateProgress(
                        leadId, milestoneCode, action, proofDocId, proofFileLink, note
                )
        );
    }

    
     // Giai đoạn 3 — API lấy toàn bộ step đã tạo của lead
     
    @GetMapping("/{leadId}/progress")
    public ResponseEntity<?> getLeadProgress(
            @PathVariable("leadId") String leadId
    ) {
        return ResponseEntity.ok(
                progressService.getLeadMilestones(leadId)
        );
    }

    /**
     * Cập nhật thông tin hồ sơ (trừ số điện thoại).
     */
    @PatchMapping("/{leadId}")
    public ResponseEntity<LeadDetailResponse> updateLeadInfo(
            @PathVariable("leadId") String leadId,
            @RequestBody UpdateLeadInfoRequest request
    ) {
        UUID leadUuid = UUID.fromString(leadId);
        Lead lead = leadRepository.findById(leadUuid)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (request.getFullName() != null) lead.setFullName(request.getFullName());
        if (request.getEmail() != null) lead.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        if (request.getMbRefId() != null) lead.setMbRefId(request.getMbRefId());
        if (request.getBusinessAddress() != null) lead.setBusinessAddress(request.getBusinessAddress());
        if (request.getBusinessNameOptions() != null) lead.setBusinessNameOptions(request.getBusinessNameOptions());
        if (request.getCharterCapital() != null) lead.setCharterCapital(request.getCharterCapital());
        if (request.getIndustryNeeds() != null) lead.setIndustryNeeds(request.getIndustryNeeds());
        if (request.getAssignedToOrg() != null) lead.setAssignedToOrg(request.getAssignedToOrg());

        leadRepository.save(lead);

        Order order = orderRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream().findFirst().orElse(null);

        LeadDetailResponse resp = LeadDetailResponse.builder()
                .id(lead.getId().toString())
                .trackingToken(lead.getTrackingToken() != null ? lead.getTrackingToken().toString() : null)
                .fullName(lead.getFullName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .mbRefId(lead.getMbRefId())
                .status(lead.getStatus())
                .assignedToOrg(lead.getAssignedToOrg())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .isDuplicate(lead.getIsDuplicate())
                .businessAddress(lead.getBusinessAddress())
                .businessNameOptions(lead.getBusinessNameOptions())
                .charterCapital(lead.getCharterCapital())
                .industryNeeds(lead.getIndustryNeeds())
                .packageCode(order != null ? order.getPackageCode() : null)
                .packageAmount(order != null ? order.getAmount() : null)
                .paymentStatus(order != null ? order.getPaymentStatus() : null)
                .orderId(order != null ? order.getId() : null)
                .build();

        return ResponseEntity.ok(resp);
    }

}
