package com.example.user_portal.entity;

import com.example.user_portal.enums.Org;
import com.example.user_portal.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Org org;

    @Column(nullable = false)
    private boolean forceChangePassword = true;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "initial_password_hash", nullable = false)
    private String initialPasswordHash;

    @CreationTimestamp
    private Instant createdAt;

    // No-arg constructor
    public User() {}

    // All-arg constructor (excluding id and createdAt, as they are generated)
    public User(String username, String email, String passwordHash, Role role, Org org, boolean forceChangePassword, boolean isActive) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.org = org;
        this.forceChangePassword = forceChangePassword;
        this.isActive = isActive;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public boolean isForceChangePassword() {
        return forceChangePassword;
    }

    public void setForceChangePassword(boolean forceChangePassword) {
        this.forceChangePassword = forceChangePassword;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public String getInitialPasswordHash() {
    return initialPasswordHash;
    }

    public void setInitialPasswordHash(String initialPasswordHash) {
    this.initialPasswordHash = initialPasswordHash;
    }

    // For authorities (as used in CustomUserDetails)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", org=" + org +
                ", forceChangePassword=" + forceChangePassword +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}