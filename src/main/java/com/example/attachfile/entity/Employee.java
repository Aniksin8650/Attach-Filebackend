package com.example.attachfile.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EMPLOYEE", schema = "ANIK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "EMP_ID", nullable = false, unique = true)
    private String empId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DEPARTMENT")
    private String department;

    @Column(name = "DESIGNATION")
    private String designation;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "JOINING_DATE")
    private String joiningDate;

    @Column(name = "MANAGER_NAME")
    private String managerName;

    @Column(name = "ROLE")
    private String role;      // "ADMIN" or "EMPLOYEE"

    @Column(name = "PASSWORD")
    private String password;  // BCrypt-hashed password
}
