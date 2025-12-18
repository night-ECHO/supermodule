package com.adflex.tracking.service;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.event.PaymentConfirmedEvent;
import com.adflex.profile.repository.LeadRepository;

import com.adflex.tracking.dto.PaymentCallbackRequest;
import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.entity.Order;
import com.adflex.tracking.enums.MilestoneStatus;
import com.adflex.tracking.enums.PaymentStatus;
import com.adflex.tracking.repository.LeadProgressRepository;
import com.adflex.tracking.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepo;
    private final LeadProgressRepository progressRepo;
    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Order pay(String orderId) {
        return handlePayment(orderId, true);
    }

    public Order callback(PaymentCallbackRequest request) {
        boolean success = request != null && Boolean.TRUE.equals(request.getSuccess());
        return handlePayment(request.getOrderId(), success);
    }

    private Order handlePayment(String orderId, boolean success) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!success) return order;

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            orderRepo.save(order);

            // === [LOGIC MỚI] Bắn thông báo Telegram (Kèm danh sách Addon) ===
            try {
                Lead lead = leadRepository.findById(UUID.fromString(order.getLeadId())).orElse(null);
                if (lead != null) {
                    // 1. Truy vấn DB để lấy danh sách các Addon đã tạo của Lead này
                    List<String> addons = getAddonsByLeadId(order.getLeadId());

                    // 2. Bắn Event với đầy đủ thông tin (Gói + Addons)
                    eventPublisher.publishEvent(new PaymentConfirmedEvent(
                            order.getLeadId(),
                            lead.getPhone(),
                            lead.getFullName(),
                            order.getPackageCode(),
                            addons // <-- Đã thêm tham số này để khớp với Event mới
                    ));
                    log.info("📢 Payment Confirmed Event sent for lead: {}", lead.getFullName());
                }
            } catch (Exception e) {
                log.error("Failed to publish payment event", e);
            }
        }

        // Logic mở khóa bước tiếp theo (Giữ nguyên)
        // Tìm bước DKDN để mở khóa nếu đang Waiting Payment
        LeadProgress lp = progressRepo.findByLeadIdAndMilestoneCode(order.getLeadId(), "STEP_DKDN");
        if (lp != null && lp.getStatus() == MilestoneStatus.WAITING_PAYMENT) {
            lp.setStatus(MilestoneStatus.IN_PROGRESS);
            lp.setStartedAt(LocalDateTime.now());
            progressRepo.save(lp);
            log.info("🔓 Unlocked STEP_DKDN for lead {}", order.getLeadId());
        }

        return order;
    }

    /**
     * Hàm phụ trợ: Lấy danh sách tên Addon từ bảng LeadProgress
     * Ví dụ: Tìm thấy "ADDON_WEB", "ADDON_ZALO" -> Trả về list ["WEB", "ZALO"]
     */
    private List<String> getAddonsByLeadId(String leadId) {
        try {
            // Giả sử repository có hàm findByLeadId... (dùng hàm có sẵn mà ProgressService đã dùng)
            List<LeadProgress> progressList = progressRepo.findByLeadIdOrderByCreatedAtAsc(leadId);

            if (progressList == null) return Collections.emptyList();

            return progressList.stream()
                    .map(LeadProgress::getMilestoneCode)       // Lấy mã bước (ví dụ ADDON_WEB)
                    .filter(code -> code.startsWith("ADDON_")) // Chỉ lấy cái nào là Addon
                    .map(code -> code.replace("ADDON_", ""))   // Cắt bỏ tiền tố để lấy tên gọn
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not retrieve addons for lead {}", leadId);
            return Collections.emptyList();
        }
    }
}