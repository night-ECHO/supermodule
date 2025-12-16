package com.example.user_portal.controller;

import com.example.user_portal.dto.CreateUserRequest;
import com.example.user_portal.dto.CreateUserResponse;
import com.example.user_portal.entity.User;
import com.example.user_portal.repository.UserRepository;
import com.example.user_portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;

    @Autowired                 
    private UserRepository userRepository;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<String> resetPassword(@PathVariable UUID id) {
        userService.resetPassword(id);
        return ResponseEntity.ok("Password has been reset to initial value");
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}