package com.example.attachfile.dto;

import java.util.List;

import lombok.Data;

@Data
public class LoginResponse {

    private String empId;
    private String name;
    private String email;
    private String phone;

    // Password policy
    private boolean passwordExpiringSoon;
    private Long daysToPasswordExpiry;
    private String lastPasswordChangeDate;

    // NEW
    private List<UserRoleDTO> roles;
}

