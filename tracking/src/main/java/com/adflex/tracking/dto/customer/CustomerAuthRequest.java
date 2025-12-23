package com.adflex.tracking.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerAuthRequest {
    @JsonProperty("access_code")
    private String accessCode;
}
