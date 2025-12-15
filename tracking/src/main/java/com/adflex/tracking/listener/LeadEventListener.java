package com.adflex.tracking.listener;

// Import Event từ module Profile (nhờ dependency trong pom.xml mà ta thấy được file này)
import com.adflex.profile.event.LeadCreatedEvent;
import com.adflex.tracking.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadEventListener {

    private final ProgressService progressService;

    // Đây là hàm lắng nghe: Khi Profile bắn event LeadCreatedEvent, hàm này tự chạy
    @Async
    @EventListener
    public void handleLeadCreated(LeadCreatedEvent event) {
        log.info("📢 [TRACKING] Nhận được tín hiệu Lead mới: {} - {}", event.leadId(), event.name());

        try {
            // Gọi logic tạo Milestone STEP_CONSULT bên Tracking
            progressService.onLeadCreated(event.leadId());
            log.info("✅ Đã khởi tạo quy trình Tracking thành công!");
        } catch (Exception e) {
            log.error("❌ Lỗi khi khởi tạo Tracking: ", e);
        }
    }
}