package com.adflex.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.adflex", "com.example.user_portal", "com.pay.payment"})  // Add this
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = {"com.adflex", "com.example.user_portal", "com.pay.payment"})     // Add this too
@EntityScan(basePackages = {"com.adflex", "com.example.user_portal", "com.pay.payment"})                    // And this
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}