package com.adflex.tracking.service;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.event.PaymentConfirmedEvent;
import com.adflex.profile.event.PaymentWaitingEvent; 
import com.adflex.profile.repository.LeadRepository;

import com.adflex.tracking.dto.LeadMilestoneDto;
import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.entity.MilestoneConfig;
import com.adflex.tracking.entity.Order;
import com.adflex.tracking.entity.Package;
import com.adflex.tracking.enums.*;
import com.adflex.tracking.repository.LeadProgressRepository;
import com.adflex.tracking.repository.MilestoneConfigRepository;
import com.adflex.tracking.repository.OrderRepository;
import com.adflex.tracking.repository.PackageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;                           // <-- đã đúng chính tả
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;                                    // <-- mới thêm
import java.nio.charset.StandardCharsets;                      // <-- mới thêm
import java.time.Instant;                                      // <-- mới thêm
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final MilestoneConfigRepository configRepo;
    private final LeadProgressRepository progressRepo;
    private final OrderRepository orderRepo;
    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PackageRepository packageRepository;
    private final OrderRepository orderRepository; // vẫn giữ (duplicate nhưng không ảnh hưởng)

    // --- 1. HÀM TẠO LEAD (Trigger tự động từ Event) ---
    @Transactional
    public LeadProgress onLeadCreated(String leadId) {
        LeadProgress exist = progressRepo.findByLeadIdAndMilestoneCode(leadId, "STEP_CONSULT");
        if (exist != null) return exist;

        LeadProgress lp = LeadProgress.builder()
                .leadId(leadId)
                .milestoneCode("STEP_CONSULT")
                .status(MilestoneStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        progressRepo.save(lp);
        log.info("✔ Created STEP_CONSULT for lead {}", leadId);
        return lp;
    }

    // --- 2. PHIÊN BẢN MỚI confirmPackage (thay thế hoàn toàn phiên bản cũ) ---
    @Transactional
    public Map<String, Object> confirmPackage(
            String leadId,
            String packageCode,
            List<String> addons,
            Boolean isPaid) {

        Lead lead = leadRepository.findById(UUID.fromString(leadId))
                .orElseThrow(() -> new RuntimeException("Lead not found: " + leadId));

        // 1. Tính total_amount server-side từ table packages
        BigDecimal total = BigDecimal.ZERO;
        if (packageCode != null && !packageCode.isBlank()) {
            Package mainPkg = packageRepository.findById(packageCode)
                    .orElseThrow(() -> new RuntimeException("Invalid package code: " + packageCode));
            total = total.add(mainPkg.getPrice());
        }

        List<String> addonList = addons != null ? addons : List.of();
        for (String addonCode : addonList) {
            Package addon = packageRepository.findById(addonCode)
                    .orElseThrow(() -> new RuntimeException("Invalid addon code: " + addonCode));
            total = total.add(addon.getPrice());
        }

        // 2. Tạo hoặc update Order (1-1 với lead)
        Order order = orderRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream().findFirst()
                .orElseGet(Order::new);

        if (order.getId() == null) {
            order.setId(UUID.randomUUID().toString());
            order.setLeadId(leadId);
        }
        order.setPackageCode(packageCode);
        order.setAddons(addonList);
        order.setTotalAmount(total);
        order.setPublicToken(UUID.randomUUID());
        // payment_status
        if (Boolean.TRUE.equals(isPaid)) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }
        order.setContractStatus(ContractStatus.PENDING);
        order.setTotalAmount(total);
if (total != null) {
    order.setAmount(total.longValue());
} else {
    order.setAmount(0L);
}
        orderRepository.save(order);

        log.info("Saved full order for lead {}: package={}, addons={}, total={}", leadId, packageCode, addonList, total);

        // 3. Publish event
        String pkgCodeForEvent = (packageCode != null && !packageCode.isBlank()) ? packageCode : "NO_PACKAGE";
        if (!Boolean.TRUE.equals(isPaid)) {
            eventPublisher.publishEvent(new PaymentWaitingEvent(
                    leadId,
                    lead.getPhone(),
                    lead.getFullName(),
                    pkgCodeForEvent,
                    addonList
            ));
        } else {
            eventPublisher.publishEvent(new PaymentConfirmedEvent(
                    leadId,
                    lead.getPhone(),
                    lead.getFullName(),
                    pkgCodeForEvent,
                    addonList
            ));
        }

        // 4. Giữ nguyên logic cũ: unlock STEP_CONSULT và tạo các milestone (core + addon)
        // -----------------------------------------------------------------------
        // Hoàn thành bước tư vấn
        LeadProgress consult = progressRepo.findByLeadIdAndMilestoneCode(leadId, "STEP_CONSULT");
        if (consult == null) consult = onLeadCreated(leadId);
        consult.setStatus(MilestoneStatus.COMPLETED);
        consult.setCompletedAt(LocalDateTime.now());
        progressRepo.save(consult);

        // Xác định level gói (nếu có) để filter core steps
        int level = (packageCode != null && packageCode.equalsIgnoreCase("GOI_2")) ? 2 : 1;

        // Tạo các bước Core
        List<MilestoneConfig> allConfigs = configRepo.findAll();
        List<MilestoneConfig> coreSteps = allConfigs.stream()
                .filter(c -> c.getType() == MilestoneType.CORE)
                .filter(c -> c.getMinPackageLevel() <= level)
                .filter(c -> !c.getCode().equals("STEP_CONSULT"))
                .sorted(Comparator.comparingInt(MilestoneConfig::getSequenceOrder))
                .toList();

        List<LeadProgress> created = new ArrayList<>();
        for (MilestoneConfig cfg : coreSteps) {
            if (progressRepo.findByLeadIdAndMilestoneCode(leadId, cfg.getCode()) != null) continue;

            MilestoneStatus status;
            if (cfg.getSequenceOrder() == 2) { // Bước tiếp theo (thường là ĐKKD)
                status = (cfg.getPaymentRequired() && !Boolean.TRUE.equals(isPaid))
                        ? MilestoneStatus.WAITING_PAYMENT
                        : MilestoneStatus.IN_PROGRESS;
            } else {
                status = MilestoneStatus.LOCKED;
            }

            LeadProgress lp = LeadProgress.builder()
                    .leadId(leadId)
                    .milestoneCode(cfg.getCode())
                    .status(status)
                    .startedAt(status == MilestoneStatus.IN_PROGRESS ? LocalDateTime.now() : null)
                    .build();
            progressRepo.save(lp);
            created.add(lp);
        }

        // Tạo Addon (nếu có). Normalize addon codes and try several candidates to match MilestoneConfig
        for (String addonCode : addonList) {
            if (addonCode == null) continue;
            // Normalize: strip possible leading ADDON_ and uppercase
            String base = addonCode.toUpperCase().replaceFirst("^ADDON_", "");

            List<String> candidates = new ArrayList<>();
            candidates.add("ADDON_" + base);           // e.g. ZALO_OA -> ADDON_ZALO_OA, ADDON_TAX_3M -> ADDON_TAX_3M
            // Common fallbacks
            if (base.endsWith("_OA")) candidates.add("ADDON_" + base.replaceFirst("_OA$", "")); // ZALO_OA -> ADDON_ZALO
            if ("WEBSITE".equals(base)) candidates.add("ADDON_WEB"); // WEBSITE -> ADDON_WEB
            // Also try without prefix in case config used non-prefixed codes (unlikely but safe)
            candidates.add(base);

            MilestoneConfig cfg = null;
            String matchedCode = null;
            for (String candidate : candidates) {
                cfg = configRepo.findByCode(candidate);
                if (cfg != null) {
                    matchedCode = candidate;
                    break;
                }
            }

            if (cfg != null && progressRepo.findByLeadIdAndMilestoneCode(leadId, matchedCode) == null) {
                LeadProgress lp = LeadProgress.builder()
                        .leadId(leadId)
                        .milestoneCode(matchedCode)
                        .status(MilestoneStatus.IN_PROGRESS)
                        .startedAt(LocalDateTime.now())
                        .build();
                progressRepo.save(lp);
                created.add(lp);
            } else if (cfg == null) {
                log.warn("No milestone config found for addon code {} (tried: {})", addonCode, candidates);
            }
        }
        // -----------------------------------------------------------------------

        // Trả về response
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("publicToken", order.getPublicToken());
        response.put("totalAmount", total);
        response.put("qrUrl", generateQrUrl(order));
        response.put("steps_created", created);

        return response;
    }

    private String generateQrUrl(Order order) {
        return "/payment/qr/" + order.getPublicToken();
    }

    // --- MỚI: Xác nhận thanh toán (thường gọi từ callback payment gateway hoặc admin) ---
    @Transactional
    public void confirmPayment(String leadId, String confirmedBy) {
        Order order = orderRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Order not found for lead " + leadId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Payment already confirmed for lead {}", leadId);
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentConfirmedAt(Instant.now());      // <-- mới thêm
        order.setPaymentConfirmedBy(UUID.fromString(confirmedBy));     
        if (order.getTotalAmount() != null) {
        order.setAmount(order.getTotalAmount().longValue());
    } else {
        order.setAmount(0L); // hoặc throw exception nếu bắt buộc phải có
    }
        

        orderRepository.save(order);

        checkAndTriggerWorkflow(order);                   // <-- gọi kiểm tra workflow
    }

    // --- MỚI: Xác nhận hợp đồng (thường gọi từ admin sau khi ký) ---
    @Transactional
    public void confirmContract(String leadId) {
        Order order = orderRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Order not found for lead " + leadId));

        if (order.getContractStatus() == ContractStatus.SIGNED_HARD_COPY) {
            log.info("Contract already signed for lead {}", leadId);
            return;
        }

        order.setContractStatus(ContractStatus.SIGNED_HARD_COPY);
        orderRepository.save(order);

        checkAndTriggerWorkflow(order);                   // <-- gọi kiểm tra workflow
    }

    // --- Phương thức kiểm tra điều kiện và trigger LeadReadyEvent ---
    private void checkAndTriggerWorkflow(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID
                && order.getContractStatus() == ContractStatus.SIGNED_HARD_COPY) {

            Lead lead = leadRepository.findById(UUID.fromString(order.getLeadId()))
                    .orElseThrow(() -> new RuntimeException("Lead not found: " + order.getLeadId()));

            eventPublisher.publishEvent(new PaymentConfirmedEvent(
                    order.getLeadId(),
                    lead.getPhone(),
                    lead.getFullName(),
                    order.getPackageCode(),
                    order.getAddons()
            ));

            log.info("LeadReadyEvent published for lead {}", order.getLeadId());
        }
    }

    // --- 3. Các hàm bổ trợ (giữ nguyên) ---
    @Transactional
    public Object updateProgress(String leadId, String milestoneCode, String action, String proofDocId, String proofFileLink, String note) {
        LeadProgress lp = progressRepo.findByLeadIdAndMilestoneCode(leadId, milestoneCode);
        if (lp == null) throw new RuntimeException("Không tìm thấy milestone!");
        MilestoneConfig cfg = configRepo.findByCode(milestoneCode);
        if (cfg == null) throw new RuntimeException("Config không tồn tại!");

        switch (action.toUpperCase()) {
            case "START" -> {
                if (cfg.getType() == MilestoneType.CORE && !canStartCoreStep(leadId, cfg)) {
                    throw new RuntimeException("Không thể START vì step trước chưa COMPLETE.");
                }
                lp.setStatus(MilestoneStatus.IN_PROGRESS);
                lp.setStartedAt(LocalDateTime.now());
            }
            case "COMPLETE" -> {
                if (cfg.getRequiredProof() && (proofDocId == null || proofDocId.isBlank())) {
                    throw new RuntimeException("Bước này cần upload proof.");
                }
                lp.setStatus(MilestoneStatus.COMPLETED);
                lp.setCompletedAt(LocalDateTime.now());
                lp.setProofDocId(proofDocId);
                if (proofDocId != null && !proofDocId.isBlank()) {
                    String link = (proofFileLink != null && !proofFileLink.isBlank())
                            ? proofFileLink
                            : "/api/admin/proofs/" + proofDocId;
                    lp.setFileLink(link);
                }
                lp.setNote(note);
                if (cfg.getType() == MilestoneType.CORE) unlockNextCoreStep(leadId, cfg);
            }
            case "FAIL" -> {
                lp.setStatus(MilestoneStatus.FAILED);
                lp.setCompletedAt(LocalDateTime.now());
            }
            default -> throw new RuntimeException("Action không hợp lệ: " + action);
        }
        progressRepo.save(lp);
        return lp;
    }

    private boolean canStartCoreStep(String leadId, MilestoneConfig cfg) {
        if (cfg.getSequenceOrder() == 1) return true;
        int prevSeq = cfg.getSequenceOrder() - 1;
        MilestoneConfig prevCfg = configRepo.findCoreBySequence(prevSeq);
        if (prevCfg == null) return false;
        LeadProgress prev = progressRepo.findByLeadIdAndMilestoneCode(leadId, prevCfg.getCode());
        return prev != null && prev.getStatus() == MilestoneStatus.COMPLETED;
    }

    private void unlockNextCoreStep(String leadId, MilestoneConfig cfg) {
        int nextSeq = cfg.getSequenceOrder() + 1;
        MilestoneConfig nextCfg = configRepo.findCoreBySequence(nextSeq);
        if (nextCfg == null) return;
        LeadProgress nextStep = progressRepo.findByLeadIdAndMilestoneCode(leadId, nextCfg.getCode());
        if (nextStep == null) return;
        if (nextStep.getStatus() == MilestoneStatus.LOCKED || nextStep.getStatus() == MilestoneStatus.WAITING_PAYMENT) {
            nextStep.setStatus(MilestoneStatus.IN_PROGRESS);
            nextStep.setStartedAt(LocalDateTime.now());
            progressRepo.save(nextStep);
        }
    }

    public List<LeadMilestoneDto> getLeadMilestones(String leadId) {
        var configs = configRepo.findAll();
        var configMap = configs.stream().collect(Collectors.toMap(MilestoneConfig::getCode, c -> c));
        return progressRepo.findByLeadIdOrderByCreatedAtAsc(leadId).stream()
                .map(lp -> {
                    MilestoneConfig cfg = configMap.get(lp.getMilestoneCode());
                    return LeadMilestoneDto.builder()
                            .milestoneCode(lp.getMilestoneCode())
                            .milestoneName(cfg != null ? cfg.getName() : null)
                            .milestoneType(cfg != null ? cfg.getType().name() : null)
                            .status(lp.getStatus())
                            .sequenceOrder(cfg != null ? cfg.getSequenceOrder() : null)
                            .slaHours(cfg != null ? cfg.getSlaHours() : null)
                            .requiredProof(cfg != null ? cfg.getRequiredProof() : null)
                            .paymentRequired(cfg != null ? cfg.getPaymentRequired() : null)
                            .startedAt(lp.getStartedAt())
                            .completedAt(lp.getCompletedAt())
                            .createdAt(lp.getCreatedAt())
                            .updatedAt(lp.getUpdatedAt())
                            .proofDocId(lp.getProofDocId())
                            .fileLink(lp.getFileLink())
                            .note(lp.getNote())
                            .build();
                })
                .toList();
    }
}