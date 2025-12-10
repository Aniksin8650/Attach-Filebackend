package com.example.attachfile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "V_DNETSNAPVIEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "F_CPERSNO", nullable = false, length = 6)
    private String empId;

    private String name;
    private String department;
    private String designation;

    private String email;
    private String phone;
    private String address;

    private String joiningDate;
    private String managerName;

    private String role;        // ADMIN / EMPLOYEE

    // ❌ REMOVE the employeePassword field
}
