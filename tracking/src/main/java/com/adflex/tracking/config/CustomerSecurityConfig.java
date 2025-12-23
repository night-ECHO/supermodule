package com.adflex.tracking.config;

import com.adflex.tracking.security.CustomerJwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class CustomerSecurityConfig {

    private final CustomerJwtAuthenticationFilter customerJwtAuthenticationFilter;

    public CustomerSecurityConfig(CustomerJwtAuthenticationFilter customerJwtAuthenticationFilter) {
        this.customerJwtAuthenticationFilter = customerJwtAuthenticationFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain customerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/customer/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/customer/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
