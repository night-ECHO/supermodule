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

    private void saveIfMissing(MilestoneConfig config) {
        if (repo.findByCode(config.getCode()) != null) return;
        repo.save(config);
    }
}
