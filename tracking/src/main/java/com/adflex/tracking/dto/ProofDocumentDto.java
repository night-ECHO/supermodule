package com.adflex.tracking.dto;

import com.adflex.tracking.config.DateTimeConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProofDocumentDto {
    private String id;
    private String name;
    private String milestoneCode;
    private String fileLink;
    private boolean isPublic;
    private Long size;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_PATTERN, timezone = DateTimeConstants.TIMEZONE)
    private Instant uploadedAt;
}
