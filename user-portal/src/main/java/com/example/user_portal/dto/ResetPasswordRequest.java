// package com.example.user_portal.dto;

// import java.util.Objects;

// public class ResetPasswordRequest {
//     private String newPassword;  // Or auto-generate

//     // No-arg constructor
//     public ResetPasswordRequest() {}

//     // All-arg constructor
//     public ResetPasswordRequest(String newPassword) {
//         this.newPassword = newPassword;
//     }

//     // Getters and Setters
//     public String getNewPassword() {
//         return newPassword;
//     }

//     public void setNewPassword(String newPassword) {
//         this.newPassword = newPassword;
//     }

//     // equals, hashCode, toString
//     @Override
//     public boolean equals(Object o) {
//         if (this == o) return true;
//         if (o == null || getClass() != o.getClass()) return false;
//         ResetPasswordRequest that = (ResetPasswordRequest) o;
//         return Objects.equals(newPassword, that.newPassword);
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(newPassword);
//     }

//     @Override
//     public String toString() {
//         return "ResetPasswordRequest{" +
//                 "newPassword='" + newPassword + '\'' +
//                 '}';
//     }
// }