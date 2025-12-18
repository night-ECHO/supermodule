package com.adflex.tracking.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CustomerDocumentDto {
    private String id;
    private String name;
    private String type;
    private Instant date;

    @JsonProperty("milestone_code")
    private String milestoneCode;
}
