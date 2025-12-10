package com.example.attachfile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "PASSWORD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePassword {

    @Id
    @Column(name = "F_PERSNO", nullable = false, length = 15)
    private String empId;  // same value as Employee.empId (F_CPERSNO)

    // Read-only link to Employee (no shared ID magic)
    @OneToOne
    @JoinColumn(
        name = "F_PERSNO",
        referencedColumnName = "F_CPERSNO",
        insertable = false,
        updatable = false
    )
    private Employee employee;

    @Column(name = "PASSWORD")
    private String passwordHash;

    @Column(name = "PASS_CHNGE_DATE")
    private LocalDate passChangeDate;
}
