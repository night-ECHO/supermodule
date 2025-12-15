package com.adflex.tracking.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfirmPackageRequest {
    private String packageCode;
    private List<String> addons;
    private Boolean isPaid;
}
