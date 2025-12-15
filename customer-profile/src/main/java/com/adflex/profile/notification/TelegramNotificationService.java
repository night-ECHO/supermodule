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
        // 1. Lấy tên gói chính
        String pkg = e.getExtra() != null ? (String) e.getExtra().get("package") : "N/A";

        // 2. Lấy danh sách Addon và format
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
        // 1. Lấy tên gói chính
        String pkg = e.getExtra() != null ? (String) e.getExtra().get("package") : "N/A";

        // 2. Lấy danh sách Addon và format
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

    // --- HÀM BỔ TRỢ ---

    // Xử lý việc nối chuỗi gói + addons (Ví dụ: "GOI_2 + WEB + ZALO")
    private String formatPackageWithAddons(String mainPackage, NotificationEvent e) {
        if (e.getExtra() == null || !e.getExtra().containsKey("addons")) {
            return mainPackage;
        }

        Object addonsObj = e.getExtra().get("addons");
        if (addonsObj instanceof List<?> list && !list.isEmpty()) {
            // Nối danh sách addon thành chuỗi
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