package com.adflex.profile.event;

/**
 * Event bắn ra khi tạo lead mới.
 * LeadService sẽ publish event này sau khi save DB.
 */
public record LeadCreatedEvent(
        String leadId,
        String phone,
        String name,
        String email
) { }
