package com.example.user_portal.service;

import com.example.user_portal.dto.LoginRequest;
import com.example.user_portal.dto.LoginResponse;
import com.example.user_portal.entity.User;
import com.example.user_portal.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDetails principal = (UserDetails) authentication.getPrincipal();
            LoginResponse response = new LoginResponse();
            if (user.isForceChangePassword()) {
                response.setToken(jwtUtil.generateToken(principal, true));  // Temp token
                response.setRequirePasswordChange(true);
            } else {
                response.setToken(jwtUtil.generateToken(principal, false));  // Full token
                response.setRequirePasswordChange(false);
            }
            return response;
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials", e);
        }
    }
}