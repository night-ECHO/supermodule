package com.adflex.profile.notification;

import java.util.Map;

public class ZaloTemplateMapping {
    public static final Map<String, String> EVENT_TO_TEMPLATE = Map.of(
            "EVENT_WELCOME", "template_id_123456",
            "EVENT_PAYMENT_PAID", "template_id_123457",
            "EVENT_MST_DONE", "template_id_123458",
            "EVENT_COMPLETED", "template_id_123459"
    );
}
