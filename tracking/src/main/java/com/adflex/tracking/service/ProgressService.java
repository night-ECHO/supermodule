package com.adflex.tracking.service;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.event.PaymentConfirmedEvent;
import com.adflex.profile.event.PaymentWaitingEvent;
import com.adflex.profile.repository.LeadRepository;

import com.adflex.tracking.dto.LeadMilestoneDto;
import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.entity.MilestoneConfig;
import com.adflex.tracking.entity.Order;
import com.adflex.tracking.enums.MilestoneStatus;
import com.adflex.tracking.enums.MilestoneType;
import com.adflex.tracking.enums.PaymentStatus;
import com.adflex.tracking.repository.LeadProgressRepository;
import com.adflex.tracking.repository.MilestoneConfigRepository;
import com.adflex.tracking.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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

    // --- 1. HÀM TẠO LEAD (Trigger tự động từ Event) ---
    @Transactional
    public LeadProgress onLeadCreated(String leadId) {
        // Kiểm tra xem đã có bước CONSULT chưa, nếu có rồi thì trả về luôn
        LeadProgress exist = progressRepo.findByLeadIdAndMilestoneCode(leadId, "STEP_CONSULT");
        if (exist != null) return exist;

        // Tạo bước đầu tiên: Tư vấn & Chốt gói
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

    // --- 2. Hàm Confirm Package (Đã cập nhật gửi list Addons) ---
    @Transactional
    public Map<String, Object> confirmPackage(
            String leadId,
            String packageCode,
            Iterable<String> addons,
            boolean isPaid
    ) {
        // --- LOGIC CŨ: Xử lý Addon Only ---
        if (packageCode == null || packageCode.isBlank()) {
            List<LeadProgress> created = new ArrayList<>();
            if (addons != null) {
                for (String addon : addons) {
                    String code = "ADDON_" + addon.toUpperCase();
                    MilestoneConfig cfg = configRepo.findByCode(code);
                    if (cfg == null) throw new RuntimeException("Không tìm thấy addon " + code);

                    LeadProgress exist = progressRepo.findByLeadIdAndMilestoneCode(leadId, code);
                    if (exist != null) continue;

                    LeadProgress lp = LeadProgress.builder()
                            .leadId(leadId)
                            .milestoneCode(code)
                            .status(MilestoneStatus.IN_PROGRESS)
                            .startedAt(LocalDateTime.now())
                            .build();
                    progressRepo.save(lp);
                    created.add(lp);
                }
            }
            return Map.of("mode", "ADDON_ONLY", "lead_id", leadId, "steps_created", created);
        }

        // --- LOGIC CŨ: Xử lý Gói 1 / Gói 2 ---
        int level = packageCode.equalsIgnoreCase("GOI_2") ? 2 : 1;

        // Hoàn thành bước tư vấn
        LeadProgress consult = progressRepo.findByLeadIdAndMilestoneCode(leadId, "STEP_CONSULT");
        if (consult == null) consult = onLeadCreated(leadId);
        consult.setStatus(MilestoneStatus.COMPLETED);
        consult.setCompletedAt(LocalDateTime.now());
        progressRepo.save(consult);

        // Tạo Order (lấy đơn mới nhất nếu đã có nhiều đơn)
        Order order = null;
        var orders = orderRepo.findByLeadIdOrderByCreatedAtDesc(leadId);
        if (orders != null && !orders.isEmpty()) {
            order = orders.get(0);
        }
        if (order == null) {
            order = Order.builder()
                    .leadId(leadId)
                    .packageCode(packageCode)
                    .amount(level == 2 ? 1_999_000L : 999_000L)
                    .paymentStatus(isPaid ? PaymentStatus.PAID : PaymentStatus.PENDING)
                    .paidAt(isPaid ? LocalDateTime.now() : null)
                    .build();
            orderRepo.save(order);
        }

        // Tạo các bước Core (ĐKKD, MST...)
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
                status = (cfg.getPaymentRequired() && !isPaid)
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

        // Tạo Addon kèm theo gói
        if (addons != null) {
            for (String addon : addons) {
                String code = "ADDON_" + addon.toUpperCase();
                MilestoneConfig cfg = configRepo.findByCode(code);
                if (cfg != null && progressRepo.findByLeadIdAndMilestoneCode(leadId, code) == null) {
                    LeadProgress lp = LeadProgress.builder()
                            .leadId(leadId)
                            .milestoneCode(code)
                            .status(MilestoneStatus.IN_PROGRESS)
                            .startedAt(LocalDateTime.now())
                            .build();
                    progressRepo.save(lp);
                    created.add(lp);
                }
            }
        }

        // --- BẮN SỰ KIỆN TELEGRAM (MỚI - Đã cập nhật Addons) ---
        try {
            // Chuyển đổi Iterable<String> sang List<String> để gửi Event
            List<String> addonList = new ArrayList<>();
            if (addons != null) {
                addons.forEach(addonList::add);
            }

            Lead lead = leadRepository.findById(UUID.fromString(leadId)).orElse(null);
            if (lead != null) {
                if (isPaid) {
                    // Đã thêm addonList vào constructor
                    eventPublisher.publishEvent(new PaymentConfirmedEvent(
                            leadId, lead.getPhone(), lead.getFullName(), packageCode, addonList
                    ));
                } else {
                    // Đã thêm addonList vào constructor
                    eventPublisher.publishEvent(new PaymentWaitingEvent(
                            leadId, lead.getPhone(), lead.getFullName(), packageCode, addonList
                    ));
                }
                log.info("📢 Sent Telegram event for lead: {}", lead.getFullName());
            }
        } catch (Exception e) {
            log.error("Failed to publish event", e);
        }

        return Map.of(
                "mode", "FULL_PACKAGE",
                "lead_id", leadId,
                "order_id", order.getId(),
                "steps_created", created
        );
    }

    // --- 3. Các hàm bổ trợ (Giữ nguyên logic của bạn) ---
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
                // Logic lưu file link như bạn đã thêm
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