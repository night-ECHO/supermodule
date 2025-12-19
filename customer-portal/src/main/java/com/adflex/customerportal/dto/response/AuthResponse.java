package com.adflex.customerportal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token; // JWT hoặc Session ID (nếu có)
    private UUID leadId;
    private String customerName;
}