// package com.tracemydata.dto;

// import java.time.LocalDateTime;

// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;

// // TempUserDTO.java
// public class TempUserDTO {
//     private String firstName;
//     private String lastName;

//     @Email(message = "Email should be valid")
//     @NotBlank(message = "Email is required")
//     private String email;

//     @NotBlank(message = "Password is required")
//     @Size(min = 8, message = "Password must be at least 8 characters")
//     private String password;
//     private String token;
//     private LocalDateTime createdAt;
//     private LocalDateTime expiresAt;



//     public String getFirstName() {
//         return firstName;
//     }

//     public void setFirstName(String firstName) {
//         this.firstName = firstName;
//     }

//     public String getLastName() {
//         return lastName;
//     }

//     public void setLastName(String lastName) {
//         this.lastName = lastName;
//     }

//     public String getEmail() {
//         return email;
//     }

//     public void setEmail(String email) {
//         this.email = email;
//     }
    
//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     public String getToken() {
//         return token;
//     }

//     public void setToken(String token) {
//         this.token = token;
//     }

//     public LocalDateTime getCreatedAt() {
//         return createdAt;
//     }

//     public void setCreatedAt(LocalDateTime createdAt) {
//         this.createdAt = createdAt;
//     }

//     public LocalDateTime getExpiresAt() {
//         return expiresAt;
//     }

//     public void setExpiresAt(LocalDateTime expiresAt) {
//         this.expiresAt = expiresAt;
//     }

//     public boolean isExpired() {
//         return LocalDateTime.now().isAfter(this.expiresAt);
//     }
// }
