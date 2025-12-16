package com.example.user_portal.service;

import com.example.user_portal.dto.*;
import com.example.user_portal.entity.User;
import com.example.user_portal.enums.Org;
import com.example.user_portal.enums.Role;
import com.example.user_portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ============================================================= */
    /* 1. TẠO USER MỚI – Lưu cả initial_password_hash (hash)        */
    /* ============================================================= */
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Lấy hoặc sinh mật khẩu ban đầu
        String initialPassword = request.getInitialPassword();
        if (initialPassword == null || initialPassword.isBlank()) {
            initialPassword = generateRandomPassword();
        }

        // Hash 1 lần duy nhất cho cả 2 cột (lần đầu giống nhau)
        String hashedInitial = passwordEncoder.encode(initialPassword);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : Role.ADMIN);
        user.setOrg(request.getOrg() != null ? request.getOrg() : Org.ADFLEX);

        user.setPasswordHash(hashedInitial);           // Mật khẩu hiện tại
        user.setInitialPasswordHash(hashedInitial);    // MẬT KHẨU GỐC – KHÔNG BAO GIỜ ĐỔI
        user.setForceChangePassword(true);
        user.setIsActive(true);

        userRepository.save(user);

        // Trả về raw password CHỈ MỘT LẦN DUY NHẤT cho admin xem
        return new CreateUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                initialPassword
        );
    }

    /* ============================================================= */
    /* 2. SINH MẬT KHẨU NGẪU NHIÊN (khi admin không nhập)           */
    /* ============================================================= */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /* ============================================================= */
    /* 3. USER ĐỔI MẬT KHẨU (sau lần đăng nhập đầu / bình thường)   */
    /* ============================================================= */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request, boolean isTempToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Với temp token (lần đầu đăng nhập) → không bắt nhập old password
        if (!isTempToken) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Old password incorrect");
            }
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        if (!isPasswordValid(request.getNewPassword())) {
            throw new RuntimeException("Password must be at least 8 characters, contain uppercase letter and number");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setForceChangePassword(false);
        userRepository.save(user);
    }

    private boolean isPasswordValid(String password) {
        return password != null &&
               password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*\\d.*");
    }

    /* ============================================================= */
    /* 4. ADMIN RESET MẬT KHẨU → TRẢ VỀ MẬT KHẨU GỐC (initial)       */
    /*    Không cần body, chỉ cần userId                           */
    /* ============================================================= */
    @Transactional
    public void resetPassword(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra an toàn (tránh trường hợp cũ chưa có initial hash)
        if (user.getInitialPasswordHash() == null || user.getInitialPasswordHash().isBlank()) {
            throw new RuntimeException("Initial password hash not found for this user");
        }

        // Reset về mật khẩu ban đầu + bắt buộc đổi lại
        user.setPasswordHash(user.getInitialPasswordHash());
        user.setForceChangePassword(true);
        userRepository.save(user);
    }

    /* ============================================================= */
    /* 5. TÌM USER THEO USERNAME                                    */
    /* ============================================================= */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}