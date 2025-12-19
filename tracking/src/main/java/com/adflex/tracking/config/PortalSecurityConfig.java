package com.adflex.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(3) // Chạy sau cùng (sau Webhook và Admin)
public class PortalSecurityConfig {

    @Bean
    public SecurityFilterChain portalFilterChain(HttpSecurity http) throws Exception {
        http

                .securityMatcher(
                        "/api/auth/**",      // API Đăng nhập Admin
                        "/api/customer/**",  // API Khách hàng (Portal)
                        "/api/documents/**"  // API Tải file
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()     // Cho phép Admin login
                        .requestMatchers("/api/customer/**").permitAll() // Cho phép Khách hàng login & xem hồ sơ
                        .requestMatchers("/api/documents/**").permitAll() // Cho phép tải file

                        // Nếu sau này bạn muốn chặt chẽ hơn thì sửa ở đây, còn giờ cứ mở để test
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}