package com.adflex.tracking.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerAuthResponse {
    private String token;

    @JsonProperty("expires_in_ms")
    private long expiresInMs;
}
