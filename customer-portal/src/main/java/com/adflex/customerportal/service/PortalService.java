package com.adflex.customerportal.service;

import com.adflex.customerportal.dto.request.AuthRequest;
import com.adflex.customerportal.dto.response.AuthResponse;
import com.adflex.customerportal.dto.response.DashboardResponse;
import com.adflex.profile.entity.Lead;
import com.adflex.profile.repository.LeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PortalService {

    @Autowired
    private LeadRepository leadRepo;


    public AuthResponse authenticate(AuthRequest request) {
        // Tìm hồ sơ theo Token (Link)
        Lead lead = leadRepo.findByTrackingToken(request.getTrackingToken())
                .orElseThrow(() -> new RuntimeException("Đường dẫn hồ sơ không tồn tại!"));

        // Kiểm tra Passcode
        if (lead.getAccessCode() == null || !lead.getAccessCode().equals(request.getAccessCode())) {
            throw new RuntimeException("Mã xác thực không đúng!");
        }


        String displayName = lead.getFullName();
        if (lead.getBusinessNameOptions() != null && !lead.getBusinessNameOptions().isEmpty()) {
            displayName = lead.getBusinessNameOptions().get(0);
        }

        return AuthResponse.builder()
                .leadId(lead.getId())
                .customerName(displayName)
                .token("SAMPLE-JWT-TOKEN-" + UUID.randomUUID()) // Giả lập Token
                .build();
    }


    public DashboardResponse getDashboard(UUID leadId) {
        Lead lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ!"));


        String companyNameDisplay = "Đang cập nhật";
        if (lead.getBusinessNameOptions() != null && !lead.getBusinessNameOptions().isEmpty()) {
            companyNameDisplay = lead.getBusinessNameOptions().get(0);
        }


        String mstDisplay = lead.getTaxCode();
        if (mstDisplay == null || mstDisplay.isEmpty()) {
            mstDisplay = "Đang xử lý";
        }
        // --------------------------------------------------------------------


        List<DashboardResponse.TimelineItem> timeline = new ArrayList<>();


        timeline.add(DashboardResponse.TimelineItem.builder()
                .stepName("Tiếp nhận yêu cầu")
                .status("COMPLETED")
                .date(lead.getCreatedAt() != null ? lead.getCreatedAt().toString() : "N/A")
                .build());


        timeline.add(DashboardResponse.TimelineItem.builder()
                .stepName("Trạng thái hiện tại")
                .status("IN_PROGRESS") // Có thể đổi logic màu sắc dựa trên lead.getStatus()
                .date("Đang thực hiện")
                .build());

        // Build dữ liệu trả về
        return DashboardResponse.builder()
                .leadInfo(DashboardResponse.CompanyInfo.builder()
                        .companyName(companyNameDisplay) // Đã dùng logic lấy từ List
                        .mst(mstDisplay)                 // Đã dùng logic lấy taxCode
                        .build())
                .currentStatus(lead.getStatus().toString())
                .timeline(timeline)
                .build();
    }
}