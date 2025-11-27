package com.example.attachfile.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String empId;
    private String name;
    private String department;
    private String designation;
    private String role;
    private String phone;
    private String email;
}
