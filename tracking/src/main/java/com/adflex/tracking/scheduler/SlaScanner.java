package com.adflex.tracking.scheduler;

import com.adflex.tracking.entity.LeadProgress;
import com.adflex.tracking.entity.MilestoneConfig;
import com.adflex.tracking.enums.MilestoneStatus;
import com.adflex.tracking.repository.LeadProgressRepository;
import com.adflex.tracking.repository.MilestoneConfigRepository;
import com.adflex.tracking.service.TelegramNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScanner {

    private final LeadProgressRepository progressRepo;
    private final MilestoneConfigRepository configRepo;
    private final TelegramNotifierService telegram;

    
    @Scheduled(fixedRate = 500000)
    public void scan() {

        var items = progressRepo.findByStatus(MilestoneStatus.IN_PROGRESS);

        for (LeadProgress lp : items) {

            MilestoneConfig cfg = configRepo.findByCode(lp.getMilestoneCode());
            if (cfg == null || cfg.getSlaHours() == null) continue;
            if (lp.getStartedAt() == null) continue;

            LocalDateTime due = lp.getStartedAt().plusHours(cfg.getSlaHours());

            if (due.isBefore(LocalDateTime.now())) {
                String msg = """
                        ⚠️ *SLA quá hạn!*
                        
                        • Lead: %s
                        • Step: %s
                        • Deadline: %s
                        """.formatted(lp.getLeadId(), lp.getMilestoneCode(), due);

                telegram.sendMessage(msg);
                log.warn("SLA overdue: {}", msg);
            }
        }
    }
}
