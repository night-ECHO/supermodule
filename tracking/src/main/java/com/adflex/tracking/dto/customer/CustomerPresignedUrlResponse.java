package com.adflex.tracking.dto.customer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerPresignedUrlResponse {
    private String url;
}

