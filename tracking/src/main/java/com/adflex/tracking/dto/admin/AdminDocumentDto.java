package com.adflex.tracking.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AdminDocumentDto {
    private String id;
    private String leadId;
    private String name;
    private String type;
    private boolean isPublic;
    private Instant uploadedAt;
}

