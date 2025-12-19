package com.adflex.customerportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// 1. Quét Bean/Controller/Service ở tất cả các nơi
@ComponentScan(basePackages = {
        "com.adflex",   // Quét tracking, profile, customerportal
        "com.example"   // Quét user-portal (cái cũ)
})
// 2. Quét Database (Entity)
@EntityScan(basePackages = {
        "com.adflex",
        "com.example"
})
// 3. Quét Repository (JPA)
@EnableJpaRepositories(basePackages = {
        "com.adflex",
        "com.example"
})
public class CustomerPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerPortalApplication.class, args);
    }
}