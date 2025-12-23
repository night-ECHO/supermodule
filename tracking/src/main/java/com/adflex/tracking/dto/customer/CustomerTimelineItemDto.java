package com.adflex.tracking.dto.customer;

import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private Instant date;
    private String note;
}
