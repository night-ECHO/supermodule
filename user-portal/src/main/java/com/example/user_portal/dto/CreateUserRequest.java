package com.example.user_portal.dto;

import com.example.user_portal.enums.Org;
import com.example.user_portal.enums.Role;

import java.util.Objects;

public class CreateUserRequest {
    private String username;
    private String email;
    private Role role;
    private Org org;
    private String initialPassword;  // Null if auto-generate

    // No-arg constructor
    public CreateUserRequest() {}

    // All-arg constructor
    public CreateUserRequest(String username, String email, Role role, Org org, String initialPassword) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.org = org;
        this.initialPassword = initialPassword;
    }

    // Getters and Setters
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

    public String getInitialPassword() {
        return initialPassword;
    }

    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateUserRequest that = (CreateUserRequest) o;
        return Objects.equals(username, that.username) && Objects.equals(email, that.email) &&
                role == that.role && org == that.org && Objects.equals(initialPassword, that.initialPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, role, org, initialPassword);
    }

    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", org=" + org +
                ", initialPassword='" + initialPassword + '\'' +
                '}';
    }
}