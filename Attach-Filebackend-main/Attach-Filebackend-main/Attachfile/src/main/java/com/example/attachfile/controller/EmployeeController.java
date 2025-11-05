package com.example.attachfile.controller;

import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import org.springframework.http.HttpStatus;

import com.example.attachfile.entity.Employee;
import com.example.attachfile.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/id/{empId}")
    public Employee getEmployeeByEmpId(@PathVariable("empId") String empId) {
        System.out.println("Fetching employee with empId: " + empId);
        return employeeRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee not found for empId: " + empId ));
    }
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    @GetMapping("/count")
    public long getEmployeeCount() {
        return employeeRepository.count();
    }


}
