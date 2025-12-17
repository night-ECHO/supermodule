package com.adflex.profile.notification;

import com.adflex.profile.integration.telegram.TelegramNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final TelegramNotifier telegramNotifier;

    @Override
    public void notify(NotificationEvent event) {
        String message = buildMessage(event);
        if (message != null && !message.isBlank()) {
            telegramNotifier.sendMessage(message);
        }
    }

    private String buildMessage(NotificationEvent e) {
        if (e == null || e.getType() == null) {
            return null;
        }

        return switch (e.getType()) {
            case LEAD_NEW -> formatLeadNew(e);
            case LEAD_DUPLICATE -> formatLeadDuplicate(e);
            case TRACKING_SLA_EXCEEDED -> formatSlaExceeded(e);
            case PAYMENT_WAITING -> formatPaymentWaiting(e);
            case PAYMENT_CONFIRMED -> formatPaymentConfirmed(e);
            case LEAD_READY -> formatLeadReady(e);  // THÊM CASE NÀY
            default -> {
                // Log cảnh báo nếu có type mới mà chưa xử lý
                System.out.println("Warning: Unknown notification type: " + e.getType());
                yield "Thông báo hệ thống (chưa định dạng)";
            }
        };
    }

    private String formatLeadNew(NotificationEvent e) {
        return """
                KHÁCH HÀNG MỚI ĐĂNG KÝ
                Tên: %s
                SĐT: %s
                Email: %s
                """.formatted(n(e.getName()), n(e.getPhone()), n(e.getEmail()));
    }

    private String formatLeadDuplicate(NotificationEvent e) {
        return """
                LEAD TRÙNG
                Tên: %s
                SĐT: %s
                (Đã tồn tại trong DB)
                """.formatted(n(e.getName()), n(e.getPhone()));
    }

    private String formatSlaExceeded(NotificationEvent e) {
        String milestone = e.getExtra() != null ? (String) e.getExtra().get("milestone") : "N/A";
        String deadline = e.getExtra() != null ? (String) e.getExtra().get("deadline") : "N/A";
        return """
                SLA EXCEEDED
                Khách: %s (%s)
                Milestone: %s
                Deadline: %s
                """.formatted(n(e.getName()), n(e.getPhone()), milestone, deadline);
    }

    private String formatPaymentWaiting(NotificationEvent e) {
        String pkg = e.getExtra() != null ? (String) e.getExtra().get("package") : "N/A";
        String fullPackageName = formatPackageWithAddons(pkg, e);

        return """
                CHỜ THANH TOÁN
                Khách: %s (%s)
                Dịch vụ: %s
                """.formatted(n(e.getName()), n(e.getPhone()), fullPackageName);
    }

    private String formatPaymentConfirmed(NotificationEvent e) {
        String pkg = e.getExtra() != null ? (String) e.getExtra().get("package") : "N/A";
        String fullPackageName = formatPackageWithAddons(pkg, e);

        return """
                THANH TOÁN THÀNH CÔNG
                Khách: %s (%s)
                Dịch vụ: %s
                Hệ thống đã ghi nhận doanh thu và mở khóa bước tiếp theo.
                """.formatted(
                n(e.getName()),
                n(e.getPhone()),
                fullPackageName
        );
    }

    private String formatLeadReady(NotificationEvent e) {
        return """
                HỒ SƠ ĐỦ ĐIỀU KIỆN XỬ LÝ
                Khách: %s
                SĐT: %s
                Đã nhận đủ thanh toán + hợp đồng bản cứng
                Ultra/AdFlex bắt đầu triển khai dịch vụ!
                """.formatted(n(e.getName()), n(e.getPhone()));
    }

    // --- HÀM BỔ TRỢ ---

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
}