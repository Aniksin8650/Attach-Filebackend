package com.example.attachfile.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String empId;
    private String oldPassword;
    private String newPassword;
}
