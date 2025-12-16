package com.adflex.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(1)  // Ưu tiên CAO NHẤT, chạy TRƯỚC tất cả các config khác
public class WebhookSecurityConfig {

    @Bean
    public SecurityFilterChain webhookFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/webhooks/**")  // Chỉ áp dụng cho các endpoint webhook
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // Cho phép tất cả, không cần JWT
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));

        return http.build();
    }
}