package com.adflex.profile.event;
import java.util.List;
/**
 * Event bắn ra khi thanh toán đã được xác nhận.
 */
public record PaymentConfirmedEvent(
        String leadId,
        String phone,
        String name,
        String packageCode,
        List<String> addons
) { }
