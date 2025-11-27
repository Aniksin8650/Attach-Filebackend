package com.example.attachfile.repository;

import com.example.attachfile.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional <Employee> findByEmpId(String empId);
}
