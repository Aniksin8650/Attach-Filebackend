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
    
    // New fields for password policy
    private boolean passwordExpiringSoon;   // true if < =7 days left
    private Long daysToPasswordExpiry;      // can be null if not applicable
    
    private String lastPasswordChangeDate; // formatted from PASS_CHNGE_DATE

}
