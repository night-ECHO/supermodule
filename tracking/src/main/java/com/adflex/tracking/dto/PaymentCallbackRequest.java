package com.adflex.tracking.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {
    private String orderId;
    private Boolean success;
}
