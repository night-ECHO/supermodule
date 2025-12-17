package com.adflex.profile.event;

/**
 * Event được publish khi Order đã PAID + SIGNED_HARD_COPY
 * → Listener ở module customer-profile sẽ bắt và gửi Telegram group: "Hồ sơ đủ điều kiện"
 */
public record LeadReadyEvent(
        String leadId,
        String phone,
        String fullName
) { }