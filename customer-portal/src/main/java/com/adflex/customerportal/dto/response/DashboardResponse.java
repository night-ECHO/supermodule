package com.adflex.customerportal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private CompanyInfo leadInfo;
    private String currentStatus;
    private List<TimelineItem> timeline;

    @Data
    @Builder
    public static class CompanyInfo {
        private String companyName;
        private String mst;
    }

    @Data
    @Builder
    public static class TimelineItem {
        private String stepName;
        private String status;
        private String date;
    }
}