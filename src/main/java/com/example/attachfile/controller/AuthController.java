package com.example.attachfile.controller;

import com.example.attachfile.dto.ChangePasswordRequest;
import com.example.attachfile.dto.LoginRequest;
import com.example.attachfile.dto.LoginResponse;
import com.example.attachfile.dto.RegisterRequest;
import com.example.attachfile.entity.Employee;
import com.example.attachfile.entity.EmployeePassword;
import com.example.attachfile.repository.EmployeePasswordRepository;
import com.example.attachfile.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final EmployeePasswordRepository employeePasswordRepository;
    private final PasswordEncoder passwordEncoder;

    /* ----------------------------------------------------
     * REGISTER
     * ---------------------------------------------------- */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        Optional<Employee> optEmp = employeeRepository.findByEmpId(req.getEmpId());
        if (optEmp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid Employee ID");
        }

        Employee emp = optEmp.get();

        // Check if PASSWORD table already has entry
        boolean alreadyRegistered = employeePasswordRepository.existsById(emp.getEmpId());
        if (alreadyRegistered) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Employee already registered");
        }

        String encoded = passwordEncoder.encode(req.getPassword());

        EmployeePassword empPass = new EmployeePassword();
        empPass.setEmpId(emp.getEmpId());
        empPass.setPasswordHash(encoded);
        empPass.setPassChangeDate(LocalDate.now());

        employeePasswordRepository.save(empPass);

        return ResponseEntity.ok("Registration successful");
    }

    /* ----------------------------------------------------
     * LOGIN
     * ---------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        Optional<Employee> optEmp = employeeRepository.findByEmpId(req.getEmpId());
        if (optEmp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee not found. Please check your Employee ID.");
        }

        Employee emp = optEmp.get();

        // Load credentials from PASSWORD table
        Optional<EmployeePassword> optPass = employeePasswordRepository.findById(emp.getEmpId());
        if (optPass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Employee is not registered. Please register first.");
        }

        EmployeePassword empPass = optPass.get();

        // 1) Validate password
        if (!passwordEncoder.matches(req.getPassword(), empPass.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        // 2) Role check
        if (req.getRole() != null &&
            emp.getRole() != null &&
            !emp.getRole().equalsIgnoreCase(req.getRole())) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Role mismatch. Please select correct role.");
        }

        // 3) Password expiry
        LocalDate today = LocalDate.now();
        LocalDate lastChange = empPass.getPassChangeDate();

        // If NULL, set today so 3-month cycle starts now
        if (lastChange == null) {
            lastChange = today;
            empPass.setPassChangeDate(today);
            employeePasswordRepository.save(empPass);
        }

        LocalDate expiryDate = lastChange.plusMonths(3);
        long daysToExpiry = ChronoUnit.DAYS.between(today, expiryDate);

        // If expired -> lock
        if (daysToExpiry < 0) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Your password has expired and your account is locked. Please change your password or contact admin.");
        }

        boolean expiringSoon = daysToExpiry <= 7;

        // FORMAT last change date for UI
        String lastChangeStr = lastChange.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        /* Build response */
        LoginResponse resp = new LoginResponse();
        resp.setEmpId(emp.getEmpId());
        resp.setName(emp.getName());
        resp.setDepartment(emp.getDepartment());
        resp.setDesignation(emp.getDesignation());
        resp.setRole(emp.getRole());
        resp.setPhone(emp.getPhone());
        resp.setEmail(emp.getEmail());

        resp.setPasswordExpiringSoon(expiringSoon);
        resp.setDaysToPasswordExpiry(daysToExpiry);
        resp.setLastPasswordChangeDate(lastChangeStr);

        return ResponseEntity.ok(resp);
    }


    /* ----------------------------------------------------
     * CHANGE PASSWORD
     * ---------------------------------------------------- */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {

        Optional<Employee> optEmp = employeeRepository.findByEmpId(req.getEmpId());
        if (optEmp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee not found. Please check your Employee ID.");
        }

        Employee emp = optEmp.get();

        Optional<EmployeePassword> optPass = employeePasswordRepository.findById(emp.getEmpId());
        if (optPass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Employee is not registered. Please register first.");
        }

        EmployeePassword empPass = optPass.get();

        // 1) Validate old password
        if (!passwordEncoder.matches(req.getOldPassword(), empPass.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Current password is incorrect");
        }

        // 2) Prevent reusing the same password
        if (passwordEncoder.matches(req.getNewPassword(), empPass.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("New password must be different from the current password");
        }

        // 3) Basic password rule
        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("New password must be at least 6 characters long");
        }

        // 4) Save new password + update date
        String encodedNew = passwordEncoder.encode(req.getNewPassword());
        empPass.setPasswordHash(encodedNew);
        empPass.setPassChangeDate(LocalDate.now());

        employeePasswordRepository.save(empPass);

        return ResponseEntity.ok("Password changed successfully");
    }

}
