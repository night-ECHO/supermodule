package com.adflex.tracking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class CustomerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomerJwtUtil customerJwtUtil;

    public CustomerJwtAuthenticationFilter(CustomerJwtUtil customerJwtUtil) {
        this.customerJwtUtil = customerJwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (customerJwtUtil.isCustomerToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID leadId = customerJwtUtil.extractLeadId(token);
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("CUSTOMER"));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(leadId, null, authorities)
                );
            }
        }
        filterChain.doFilter(request, response);
    }
}

