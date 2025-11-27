package com.example.attachfile.controller;

import com.example.attachfile.dto.LoginRequest;
import com.example.attachfile.dto.LoginResponse;
import com.example.attachfile.dto.RegisterRequest;
import com.example.attachfile.entity.Employee;
import com.example.attachfile.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        Optional<Employee> optEmp = employeeRepository.findByEmpId(req.getEmpId());
        if (optEmp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid Employee ID");
        }

        Employee emp = optEmp.get();

        if (emp.getPassword() != null && !emp.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Employee already registered");
        }

        String encoded = passwordEncoder.encode(req.getPassword());
        emp.setPassword(encoded);
        employeeRepository.save(emp);

        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Employee> optEmp = employeeRepository.findByEmpId(req.getEmpId());
        if (optEmp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee not found. Please check your Employee ID.");
        }

        Employee emp = optEmp.get();

        if (emp.getPassword() == null || emp.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Employee is not registered. Please register first.");
        }

        if (!passwordEncoder.matches(req.getPassword(), emp.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        if (req.getRole() != null &&
            emp.getRole() != null &&
            !emp.getRole().equalsIgnoreCase(req.getRole())) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Role mismatch. Please select correct role.");
        }

        LoginResponse resp = new LoginResponse();
        resp.setEmpId(emp.getEmpId());
        resp.setName(emp.getName());
        resp.setDepartment(emp.getDepartment());
        resp.setDesignation(emp.getDesignation());
        resp.setRole(emp.getRole());
        resp.setPhone(emp.getPhone());
        resp.setEmail(emp.getEmail());

        return ResponseEntity.ok(resp);
    }
}
 