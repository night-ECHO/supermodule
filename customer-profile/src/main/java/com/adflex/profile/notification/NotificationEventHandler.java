package com.adflex.profile.notification;

import com.adflex.profile.event.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;

    @EventListener
    public void onLeadCreated(LeadCreatedEvent e) {
        notificationService.notify(
                NotificationEvent.builder()
                        .type(NotificationType.LEAD_NEW)
                        .leadId(e.leadId())
                        .phone(e.phone())
                        .name(e.name())
                        .email(e.email())
                        .build()
        );
    }

    @EventListener
    public void onLeadDuplicate(LeadDuplicateEvent e) {
        notificationService.notify(
                NotificationEvent.builder()
                        .type(NotificationType.LEAD_DUPLICATE)
                        .leadId(e.leadId())
                        .phone(e.phone())
                        .name(e.name())
                        .build()
        );
    }

    @EventListener
    public void onMilestoneSlaExceeded(MilestoneSlaExceededEvent e) {
        notificationService.notify(
                NotificationEvent.builder()
                        .type(NotificationType.TRACKING_SLA_EXCEEDED)
                        .leadId(e.leadId())
                        .phone(e.phone())
                        .name(e.name())
                        .extra(Map.of(
                                "milestone", e.milestoneCode(),
                                "deadline", e.deadline().toString()
                        ))
                        .build()
        );
    }

    // --- CẬP NHẬT: Thêm addons vào map extra ---
    @EventListener
    public void onPaymentWaiting(PaymentWaitingEvent e) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("package", e.packageCode());
        // Lấy list addons từ Event đưa vào map để format tin nhắn
        extra.put("addons", e.addons());

        notificationService.notify(
                NotificationEvent.builder()
                        .type(NotificationType.PAYMENT_WAITING)
                        .leadId(e.leadId())
                        .phone(e.phone())
                        .name(e.name())
                        .extra(extra)
                        .build()
        );
    }

    // --- CẬP NHẬT: Thêm addons vào map extra ---
    @EventListener
    public void onPaymentConfirmed(PaymentConfirmedEvent e) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("package", e.packageCode());
        // Lấy list addons từ Event đưa vào map
        extra.put("addons", e.addons());

        notificationService.notify(
                NotificationEvent.builder()
                        .type(NotificationType.PAYMENT_CONFIRMED)
                        .leadId(e.leadId())
                        .phone(e.phone())
                        .name(e.name())
                        .extra(extra)
                        .build()
        );
    }
    @EventListener
public void onLeadReady(LeadReadyEvent e) {
    notificationService.notify(
            NotificationEvent.builder()
                    .type(NotificationType.LEAD_READY)
                    .leadId(e.leadId())
                    .phone(e.phone())
                    .name(e.fullName())
                    .build()
    );
}
}