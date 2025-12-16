package com.example.user_portal.dto;

import java.util.Objects;

public class LoginResponse {
    private String token;
    private boolean requirePasswordChange;

    // No-arg constructor
    public LoginResponse() {}

    // All-arg constructor
    public LoginResponse(String token, boolean requirePasswordChange) {
        this.token = token;
        this.requirePasswordChange = requirePasswordChange;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isRequirePasswordChange() {
        return requirePasswordChange;
    }

    public void setRequirePasswordChange(boolean requirePasswordChange) {
        this.requirePasswordChange = requirePasswordChange;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return requirePasswordChange == that.requirePasswordChange && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, requirePasswordChange);
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + token + '\'' +
                ", requirePasswordChange=" + requirePasswordChange +
                '}';
    }
}