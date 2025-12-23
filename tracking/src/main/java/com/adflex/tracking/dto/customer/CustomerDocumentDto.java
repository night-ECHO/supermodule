package com.adflex.tracking.dto.customer;

import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CustomerDocumentDto {
    private String id;
    private String name;
    private String type;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private Instant date;

    @JsonProperty("milestone_code")
    private String milestoneCode;
}
