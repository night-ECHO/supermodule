package com.adflex.profile.event;

import java.time.Instant;

/**
 * Event bắn ra khi một milestone nào đó vượt quá deadline SLA.
 */
public record MilestoneSlaExceededEvent(
        String leadId,
        String phone,
        String name,
        String milestoneCode,
        Instant deadline
) { }
