package com.adflex.profile.event;

import java.util.List;

/**
 * Event bắn ra khi chờ thanh toán (VD: STEP_DKDN ở trạng thái WAITING_PAYMENT).
 */
public record PaymentWaitingEvent(
        String leadId,
        String phone,
        String name,
        String packageCode,
        List<String>addons
) { }
