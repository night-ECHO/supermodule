package com.adflex.tracking; // Hoặc package hiện tại của bạn

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan; // Import mới
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // Import mới
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.adflex")
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "com.adflex")
@EntityScan(basePackages = "com.adflex")
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}