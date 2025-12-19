package com.adflex.tracking.config;

import com.adflex.tracking.entity.MilestoneConfig;
import com.adflex.tracking.enums.MilestoneType;
import com.adflex.tracking.repository.MilestoneConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MilestoneConfigSeeder implements CommandLineRunner {

    private final MilestoneConfigRepository repo;

    @Override
    public void run(String... args) {

        saveIfMissing(MilestoneConfig.builder()
                .code("STEP_CONSULT")
                .name("Tư vấn & Chốt gói")
                .type(MilestoneType.CORE)
                .sequenceOrder(1)
                .paymentRequired(false)
                .requiredProof(false)
                .slaHours(24)
                .minPackageLevel(1)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("STEP_DKDN")
                .name("Đăng ký kinh doanh")
                .type(MilestoneType.CORE)
                .sequenceOrder(2)
                .paymentRequired(true)
                .requiredProof(true)
                .slaHours(48)
                .minPackageLevel(1)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("STEP_MST")
                .name("Mã số thuế")
                .type(MilestoneType.CORE)
                .sequenceOrder(3)
                .paymentRequired(false)
                .requiredProof(true)
                .slaHours(48)
                .minPackageLevel(1)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("STEP_HDDT")
                .name("Hóa đơn điện tử")
                .type(MilestoneType.CORE)
                .sequenceOrder(4)
                .paymentRequired(false)
                .requiredProof(true)
                .slaHours(48)
                .minPackageLevel(1)
                .build());
        
                // chỉ áp dụng cho Gói 2
        saveIfMissing(MilestoneConfig.builder()
                .code("STEP_TAX_SVC")
                .name("Dịch vụ thuế")
                .type(MilestoneType.CORE)
                .sequenceOrder(5)
                .paymentRequired(false)
                .requiredProof(true     )
                .slaHours(72)
                .minPackageLevel(2) 
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("ADDON_ZALO")
                .name("Zalo OA Business")
                .type(MilestoneType.ADDON)
                .sequenceOrder(null)
                .paymentRequired(false)
                .requiredProof(false)
                .slaHours(72)
                .minPackageLevel(1)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("ADDON_WEB")
                .name("Khởi tạo Website")
                .type(MilestoneType.ADDON)
                .sequenceOrder(null)
                .paymentRequired(false)
                .requiredProof(false)
                .slaHours(120)
                .minPackageLevel(1)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("ADDON_TAX_3M")
                .name("Dịch vụ thuế >3 tháng (Gói 2, 10% commission AdFlex)")
                .type(MilestoneType.ADDON)
                .sequenceOrder(null)
                .paymentRequired(false)
                .requiredProof(false)
                .slaHours(168)
                .minPackageLevel(2)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("ADDON_GOOGLE_BUSINESS")
                .name("Tài khoản Google Business")
                .type(MilestoneType.ADDON)
                .sequenceOrder(null)
                .paymentRequired(false)
                .requiredProof(false)
                .slaHours(72)
                .minPackageLevel(1)
                .build());

        saveIfMissing(MilestoneConfig.builder()
                .code("ADDON_ZALO_MINIAPP")
                .name("MiniApp Zalo")
                .type(MilestoneType.ADDON)
                .sequenceOrder(null)
                .paymentRequired(false)
                .requiredProof(false)
                .slaHours(96)
                .minPackageLevel(1)
                .build());
    }

    private void saveIfMissing(MilestoneConfig desired) {
        MilestoneConfig existing = repo.findByCode(desired.getCode());
        if (existing == null) {
            repo.save(desired);
            return;
        }

        boolean changed = false;
        if (!equals(existing.getName(), desired.getName())) {
            existing.setName(desired.getName());
            changed = true;
        }
        if (existing.getType() != desired.getType()) {
            existing.setType(desired.getType());
            changed = true;
        }
        if (!equals(existing.getSequenceOrder(), desired.getSequenceOrder())) {
            existing.setSequenceOrder(desired.getSequenceOrder());
            changed = true;
        }
        if (!equals(existing.getPaymentRequired(), desired.getPaymentRequired())) {
            existing.setPaymentRequired(desired.getPaymentRequired());
            changed = true;
        }
        if (!equals(existing.getRequiredProof(), desired.getRequiredProof())) {
            existing.setRequiredProof(desired.getRequiredProof());
            changed = true;
        }
        if (!equals(existing.getSlaHours(), desired.getSlaHours())) {
            existing.setSlaHours(desired.getSlaHours());
            changed = true;
        }
        if (!equals(existing.getMinPackageLevel(), desired.getMinPackageLevel())) {
            existing.setMinPackageLevel(desired.getMinPackageLevel());
            changed = true;
        }

        if (changed) {
            repo.save(existing);
        }
    }

    private boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
