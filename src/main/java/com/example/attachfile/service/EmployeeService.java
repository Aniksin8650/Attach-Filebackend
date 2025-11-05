package com.example.attachfile.service;

import com.example.attachfile.entity.Employee;
import com.example.attachfile.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Fetch employee by empId
    public Employee getEmployeeByEmpId(String empId) {
        System.out.println("Fetching employee with empId: " + empId);
        return employeeRepository.findByEmpId(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found for empId: " + empId));
    }
}
