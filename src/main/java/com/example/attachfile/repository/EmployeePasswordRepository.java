package com.example.attachfile.repository;

import com.example.attachfile.entity.EmployeePassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeePasswordRepository extends JpaRepository<EmployeePassword, String> {
    // String = empId (F_PERSNO / F_CPERSNO)
}
