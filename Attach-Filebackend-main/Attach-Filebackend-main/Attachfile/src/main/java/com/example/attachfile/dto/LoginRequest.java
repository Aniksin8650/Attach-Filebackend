package com.example.attachfile.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String empId;
    private String password;
    private String role;   // "ADMIN" or "EMPLOYEE"
}
