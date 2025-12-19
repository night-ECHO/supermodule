package com.adflex.tracking.service;

import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.entity.Order;
import com.adflex.tracking.enums.PaymentStatus;
import com.adflex.tracking.repository.LeadProgressRepository;
import com.adflex.tracking.repository.OrderRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.adflex.tracking.enums.ContractStatus;
import com.adflex.tracking.enums.MilestoneStatus;
import com.adflex.profile.entity.LeadStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor

public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher; // Để publish event notify nếu cần
    private final LeadProgressRepository progressRepo;
    private final LeadRepository leadRepository; // Để update lead status nếu cần
    private final TelegramNotifierService telegramNotifier; // Hoặc ZaloNotifier nếu có

    // upload file shit
    @Value("${app.upload.contract-dir:uploads/contracts}")
    private String contractUploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(contractUploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Transactional
    public Order confirmPayment(String orderId, String confirmedByUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Payment already confirmed");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentConfirmedAt(Instant.now());

        // Convert username -> UUID nếu hệ thống dùng UUID cho user
        // Giả sử có cách lấy userId từ username, tạm hard-code hoặc inject UserService
        order.setPaymentConfirmedBy(UUID.fromString("00000000-0000-0000-0000-000000000000")); // TODO: Replace với logic
                                                                                              // lấy UUID từ
                                                                                              // principal/username

        orderRepository.save(order);

        log.info("Payment confirmed manually for order {} by {}", orderId, confirmedByUsername);

        return order;
    }

    public Order getPublicOrderByToken(UUID token) {
        return orderRepository.findByPublicToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired payment link"));
    }

    public Map<String, Object> generatePublicPaymentInfo(Order order) {
        // Config bank - hard-code tạm theo công ty AdFlex
        // Config bank công ty AdFlex - theo tìm hiểu, thường dùng Vietcombank
        String bankId = "mbbank"; // hoặc "VCB" lowercase, hoặc BIN "970436" nếu cần
        String accountNo = "0915335807"; // Ví dụ Quỹ vắc xin, THAY BẰNG TK THỰC CỦA ADFLEX
        String accountName = "CONG TY CO PHAN ADFLEX VIET NAM"; // THAY BẰNG TÊN CHỦ TK THỰC
        String template = "compact2"; // Đẹp nhất, có logo ngân hàng
        String extension = "jpg"; // hoặc png

        String addInfo = "Thanh toan HD " + order.getLeadId();

        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-%s.%s?amount=%d&addInfo=%s&accountName=%s",
                bankId,
                accountNo,
                template,
                extension,
                order.getTotalAmount().longValue(),
                URLEncoder.encode(addInfo, StandardCharsets.UTF_8),
                URLEncoder.encode(accountName, StandardCharsets.UTF_8));

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("leadId", order.getLeadId());
        response.put("packageCode", order.getPackageCode());
        response.put("addons", order.getAddons());
        response.put("totalAmount", order.getTotalAmount());
        response.put("paymentStatus", order.getPaymentStatus());
        response.put("qrCodeUrl", qrUrl);
        response.put("paymentLink", "https://portal.adflex.vn/pay/" + order.getPublicToken()); // Link chia sẻ

        return response;
    }

    public Map<String, Object> getPublicPaymentInfoByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return generatePublicPaymentInfo(order);
    }

    // TODO: Inject storage service (S3, MinIO, hoặc local) - tạm giả sử có
    // StorageService lưu file và trả URL
    @Transactional
    public Order confirmContract(String orderId, String confirmedByUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getContractStatus() == ContractStatus.SIGNED_HARD_COPY) {
            throw new RuntimeException("Contract already confirmed");
        }

        order.setContractStatus(ContractStatus.SIGNED_HARD_COPY);
        orderRepository.save(order);

        log.info("Contract confirmed manually for order {} by {}", orderId, confirmedByUsername);

        // Check both để trigger full
        checkIfBothConfirmedAndTrigger(order);

        return order;
    }

    public String uploadContractScan(String orderId, MultipartFile file, String uploadedBy) {
        if (file.isEmpty() || !StringUtils.cleanPath(file.getOriginalFilename()).endsWith(".pdf")) {
            throw new RuntimeException("File rỗng hoặc không phải PDF");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            // Tên file unique: contract_order_{orderId}_{timestamp}.pdf
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = "contract_order_" + orderId + "_" + Instant.now().toEpochMilli() + ".pdf";

            Path uploadPath = Paths.get(contractUploadDir);
            Path filePath = uploadPath.resolve(fileName);

            // Copy file vào folder
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL để xem/download (giả sử serve static từ /uploads/contracts/)
            String fileLink = "/uploads/contracts/" + fileName;

            log.info("Contract scan uploaded: {} for order {} by {}", fileLink, orderId, uploadedBy);

            // TODO: Nếu có bảng documents, save record với orderId, type=CONTRACT,
            // link=fileLink, uploadedBy

            return fileLink;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload contract scan", e);
        }
    }

    private void checkIfBothConfirmedAndTrigger(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID &&
                order.getContractStatus() == ContractStatus.SIGNED_HARD_COPY) {

            String leadId = order.getLeadId();

            // 1. Unlock STEP_DKDN (reuse logic cũ từ PaymentService)
            LeadProgress lp = progressRepo.findByLeadIdAndMilestoneCode(leadId, "STEP_DKDN");
            if (lp != null && lp.getStatus() != MilestoneStatus.COMPLETED) {
                lp.setStatus(MilestoneStatus.IN_PROGRESS);
                lp.setStartedAt(LocalDateTime.now());
                progressRepo.save(lp);
                log.info("🔓 Unlocked STEP_DKDN after both payment & contract confirmed for lead {}", leadId);
            }

            // 2. Update lead status thành PROCESSING (nếu entity Lead có field status)
            // Lead lead = leadRepository.findById(UUID.fromString(leadId)).orElse(null);
            // if (lead != null && lead.getStatus() != LeadStatus.PROCESSING) { // Giả sử có
            // field status
            // lead.setStatus(LeadStatus.PROCESSING);
            // leadRepository.save(lead);
            // }

            // 3. Notify group – SỬA THÀNH PHIÊN BẢN NÀY (an toàn null)
            String fullName = "N/A";
            String phone = "N/A";
            String packageCode = order.getPackageCode() != null ? order.getPackageCode() : "Không";
            String addonsStr = order.getAddons() != null && !order.getAddons().isEmpty()
                    ? String.join(", ", order.getAddons())
                    : "Không";

            String message = """
                    🎉 LEAD HOÀN THÀNH THANH TOÁN & HỢP ĐỒNG
                    Mã lead: %s
                    Khách: %s - %s
                    Gói: %s
                    Addons: %s
                    Tổng tiền: %,d VNĐ
                    ➜ Mở khóa Module 4: Đăng ký kinh doanh
                    """.formatted(
                    leadId,
                    fullName,
                    phone,
                    packageCode,
                    addonsStr,
                    order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0);
            telegramNotifier.sendMessage(message);

            // TODO: Nếu cần publish event LeadReadyEvent hoặc custom BothConfirmedEvent

            log.info("✅ Both payment & contract confirmed → Trigger full workflow for lead {}", leadId);
        }
    }
    // Các method khác sẽ thêm sau (confirm contract, etc.)
}