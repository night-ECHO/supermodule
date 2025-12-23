package com.adflex.tracking.config;

import com.adflex.tracking.entity.Package;
import com.adflex.tracking.enums.PackageType;
import com.adflex.tracking.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PackageDataSeeder implements CommandLineRunner {

    private final PackageRepository packageRepository;

    @Override
    public void run(String... args) {
        if (packageRepository.count() == 0) {
            log.info("Seeding package data...");

            List<Package> packages = List.of(
                    create("GOI_1", "Gói Cơ bản", "1500000", PackageType.MAIN_PACKAGE, 1),
                    create("GOI_2", "Gói Cao cấp", "3500000", PackageType.MAIN_PACKAGE, 2),
                    create("ZALO_OA", "Zalo OA", "600000", PackageType.ADDON, 1),
                    create("WEBSITE", "Website", "1200000", PackageType.ADDON, 2),
                    create("ADDON_TAX_3M", "Dịch vụ kế toán thuế 3 tháng", "900000", PackageType.ADDON, 1),
                    create("ADDON_GOOGLE_BUSINESS", "Google Business", "500000", PackageType.ADDON, 1),
                    create("ADDON_ZALO_MINIAPP", "Zalo MiniApp", "700000", PackageType.ADDON, 2)
            );

            packageRepository.saveAll(packages);
            log.info("Seeded {} packages successfully", packages.size());
        }
    }

    private Package create(String code, String name, String priceStr, PackageType type, int level) {
        Package p = new Package();
        p.setCode(code);
        p.setName(name);
        p.setPrice(new BigDecimal(priceStr));
        p.setType(type);
        p.setMinPackageLevel(level);
        return p;
    }
}