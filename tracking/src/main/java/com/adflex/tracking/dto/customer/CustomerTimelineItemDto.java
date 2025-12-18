package com.adflex.tracking.dto.customer;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CustomerTimelineItemDto {
    private String code;
    private String name;
    private String milestoneType;
    private String status;
    private Instant date;
    private String note;
}
