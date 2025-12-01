package com.example.attachfile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "EMPLOYEE")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    private String empId;

    private String name;
    private String department;
    private String designation;

    private String email;
    private String phone;
    private String address;

    private String joiningDate;  // If you want, we can convert to LocalDate later.
    private String managerName;

    private String role;        // ADMIN / EMPLOYEE
    private String password;    // Hashed password
}
