package com.adflex.profile.notification;

import com.adflex.profile.integration.telegram.TelegramNotifier;
import com.adflex.profile.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final TelegramNotifier telegramNotifier;
    private final NotificationLogRepository logRepo;
    private final ZaloZnsService zaloZnsService;

    @Override
    public void notify(NotificationEvent event) {
        if (event == null || event.getType() == null) {
            return;
        }

        // Telegram send + log
        String message = buildMessage(event);
        NotificationLog logRow = NotificationLog.builder()
                .leadId(toUUID(event.getLeadId()))
                .channel(NotificationChannel.TELEGRAM)
                .eventType(event.getType().name())
                .recipient("TELEGRAM_CHAT") // thực tế chat-id trong properties
                .contentPreview(message)
                .status(NotificationStatus.PENDING)
                .build();
        logRow = logRepo.save(logRow);

        try {
            if (message != null && !message.isBlank()) {
                telegramNotifier.sendMessage(message);
            }
            logRow.setStatus(NotificationStatus.SENT);
            logRow.setSentAt(LocalDateTime.now());
            logRepo.save(logRow);
        } catch (Exception e) {
            logRow.setStatus(NotificationStatus.FAILED);
            logRow.setErrorMessage(e.getMessage());
            logRepo.save(logRow);
        }

        // Zalo welcome only for LEAD_NEW
        if (event.getType() == NotificationType.LEAD_NEW) {
            Map<String, Object> zaloParams = Map.of(
                    "customer_name", n(event.getName()),
                    "phone", n(event.getPhone())
            );
            zaloZnsService.notifyZalo(toUUID(event.getLeadId()), event.getPhone(), "EVENT_WELCOME", zaloParams);
        }
    }

    @Override
    public void retry(NotificationLog log) {
        if (log.getChannel() == NotificationChannel.TELEGRAM) {
            NotificationEvent fakeEvent = NotificationEvent.builder()
                    .type(NotificationType.valueOf(log.getEventType()))
                    .leadId(log.getLeadId() == null ? null : log.getLeadId().toString())
                    .phone(log.getRecipient())
                    .name(log.getContentPreview())
                    .build();

            try {
                telegramNotifier.sendMessage(log.getContentPreview());
                log.setStatus(NotificationStatus.SENT);
                log.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                log.setStatus(NotificationStatus.FAILED);
                log.setErrorMessage(e.getMessage());
            }
            logRepo.save(log);
        }
    }

    private String buildMessage(NotificationEvent e) {
        return switch (e.getType()) {
            case LEAD_NEW -> formatLeadNew(e);
            case LEAD_DUPLICATE -> formatLeadDuplicate(e);
            case TRACKING_SLA_EXCEEDED -> formatSlaExceeded(e);
            case PAYMENT_WAITING -> formatPaymentWaiting(e);
            case PAYMENT_CONFIRMED -> formatPaymentConfirmed(e);
        };
    }

    private String formatLeadNew(NotificationEvent e) {
        return """
                🚀 KHÁCH HÀNG MỚI ĐĂNG KÝ
                👤 Tên: %s
                📞 SĐT: %s
                📧 Email: %s
                """.formatted(n(e.getName()), n(e.getPhone()), n(e.getEmail()));
    }

    private String formatLeadDuplicate(NotificationEvent e) {
        return """
                ⚠️ CẢNH BÁO: KHÁCH HÀNG TRÙNG LẶP
                👤 Tên: %s
                📞 SĐT: %s
                ℹ️ Khách hàng này đã tồn tại trên hệ thống.
                """.formatted(n(e.getName()), n(e.getPhone()));
    }

    private String formatSlaExceeded(NotificationEvent e) {
        Object milestone = e.getExtra() != null ? e.getExtra().get("milestone") : "N/A";
        Object deadline  = e.getExtra() != null ? e.getExtra().get("deadline")  : "N/A";

        return """
                🚨 CẢNH BÁO QUÁ HẠN XỬ LÝ (SLA)
                📌 Bước: %s
                👤 Khách: %s (%s)
                ⏳ Deadline: %s
                👉 Vui lòng kiểm tra tiến độ ngay!
                """.formatted(
                n(milestone),
                n(e.getName()),
                n(e.getPhone()),
                n(deadline)
        );
    }

    private String formatPaymentWaiting(NotificationEvent e) {
        String pkg = e.getExtra() != null ? (String) e.getExtra().get("package") : "N/A";
        String fullPackageName = formatPackageWithAddons(pkg, e);

        return """
                💸 YÊU CẦU THANH TOÁN MỚI
                👤 Khách: %s (%s)
                📦 Dịch vụ: %s
                👉 Vui lòng liên hệ khách để hoàn tất thanh toán.
                """.formatted(
                n(e.getName()),
                n(e.getPhone()),
                fullPackageName
        );
    }

    private String formatPaymentConfirmed(NotificationEvent e) {
        String pkg = e.getExtra() != null ? (String) e.getExtra().get("package") : "N/A";
        String fullPackageName = formatPackageWithAddons(pkg, e);

        return """
                🎉 THANH TOÁN THÀNH CÔNG
                👤 Khách: %s (%s)
                📦 Dịch vụ: %s
                ✅ Hệ thống đã ghi nhận doanh thu và mở khóa bước tiếp theo.
                """.formatted(
                n(e.getName()),
                n(e.getPhone()),
                fullPackageName
        );
    }

    private String formatPackageWithAddons(String mainPackage, NotificationEvent e) {
        if (e.getExtra() == null || !e.getExtra().containsKey("addons")) {
            return mainPackage;
        }

        Object addonsObj = e.getExtra().get("addons");
        if (addonsObj instanceof List<?> list && !list.isEmpty()) {
            String addonStr = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(" + "));
            return mainPackage + " + " + addonStr;
        }

        return mainPackage;
    }

    private String n(Object v) {
        return v == null ? "" : v.toString();
    }

    private UUID toUUID(String s) {
        try {
            return s == null ? null : UUID.fromString(s);
        } catch (Exception e) {
            return null;
        }
    }
}
