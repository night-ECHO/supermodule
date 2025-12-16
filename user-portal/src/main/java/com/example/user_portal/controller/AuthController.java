package com.example.user_portal.controller;

import com.example.user_portal.dto.*;
import com.example.user_portal.enums.Org;
import com.example.user_portal.enums.Role;
import com.example.user_portal.security.JwtUtil;
import com.example.user_portal.service.AuthService;
import com.example.user_portal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UserService userService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request,
                                                 @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = userService.findByUsername(auth.getName())
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        boolean isTemp = token != null && jwtUtil.isTempToken(token);

        userService.changePassword(userId, request, isTemp);
        return ResponseEntity.ok("Password changed successfully");
    }


    @PostMapping("/register-admin")
    public ResponseEntity<CreateUserResponse> registerFirstAdmin(@RequestBody CreateUserRequest request) {
        if (userService.findByUsername("admin").isPresent()) {
            throw new RuntimeException("First admin already registered.");
        }
        request.setRole(Role.ADMIN);
        request.setOrg(request.getOrg() != null ? request.getOrg() : Org.ADFLEX);

        CreateUserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }
}