package com.adflex.customerportal.dto.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String trackingToken; // Token trên URL (xyz123)
    private String accessCode;    // Mật khẩu khách nhập (888888)
}