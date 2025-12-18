package com.adflex.tracking.dto.admin;

import lombok.Data;

@Data
public class AdminUpdateDocumentRequest {
    private Boolean isPublic;
    private String name;
    private String type;
}

