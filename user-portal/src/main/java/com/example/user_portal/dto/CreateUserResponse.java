package com.example.user_portal.dto;

import java.util.Objects;
import java.util.UUID;

public class CreateUserResponse {
    private UUID id;
    private String username;
    private String email;
    private String initialPassword;  // Raw, only returned once

    // No-arg constructor
    public CreateUserResponse() {}

    // All-arg constructor
    public CreateUserResponse(UUID id, String username, String email, String initialPassword) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.initialPassword = initialPassword;
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
        CreateUserResponse that = (CreateUserResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) && Objects.equals(initialPassword, that.initialPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, initialPassword);
    }

    @Override
    public String toString() {
        return "CreateUserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", initialPassword='" + initialPassword + '\'' +
                '}';
    }
}