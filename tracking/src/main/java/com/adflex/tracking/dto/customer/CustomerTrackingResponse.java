package com.adflex.tracking.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CustomerTrackingResponse {
    @JsonProperty("lead_info")
    private Map<String, Object> leadInfo;

    @JsonProperty("current_status")
    private String currentStatus;
    private List<CustomerTimelineItemDto> timeline;
    private List<CustomerDocumentDto> documents;
}
