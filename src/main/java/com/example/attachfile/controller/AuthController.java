package com.example.attachfile.controller;

import com.example.attachfile.dto.LoginRequest;
import com.example.attachfile.dto.LoginResponse;
import com.example.attachfile.dto.UserRoleDTO;
import com.example.attachfile.entity.Employee;
import com.example.attachfile.entity.EmployeePassword;
import com.example.attachfile.entity.RolesNew;
import com.example.attachfile.repository.EmployeePasswordRepository;
import com.example.attachfile.repository.EmployeeRepository;
import com.example.attachfile.repository.RolesNewRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final EmployeePasswordRepository employeePasswordRepository;
    private final RolesNewRepository rolesNewRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    /* ----------------------------------------------------
     * LOGIN (MULTI-ROLE ENABLED)
     * ---------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        /* 1️⃣ Validate employee */
        Optional<Employee> optEmp = employeeRepository.findByEmpId(req.getEmpId());
        if (optEmp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee not found. Please check your Employee ID.");
        }

        Employee emp = optEmp.get();

        /* 2️⃣ Validate registration */
        Optional<EmployeePassword> optPass =
                employeePasswordRepository.findById(emp.getEmpId());

        if (optPass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Employee is not registered. Please register first.");
        }

        EmployeePassword empPass = optPass.get();

        /* 3️⃣ Validate password */
        if (!passwordEncoder.matches(req.getPassword(), empPass.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        /* 4️⃣ Password expiry logic */
        LocalDate today = LocalDate.now();
        LocalDate lastChange = empPass.getPassChangeDate();

        if (lastChange == null) {
            lastChange = today;
            empPass.setPassChangeDate(today);
            employeePasswordRepository.save(empPass);
        }

        LocalDate expiryDate = lastChange.plusMonths(3);
        long daysToExpiry = ChronoUnit.DAYS.between(today, expiryDate);

        if (daysToExpiry < 0) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Your password has expired and your account is locked.");
        }

        boolean expiringSoon = daysToExpiry <= 7;
        String lastChangeStr =
                lastChange.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        /* 5️⃣ Resolve PERSNO */
        String persno = emp.getEmpId().trim();

        System.out.println(">>> FETCHING ROLES FOR PERSNO = [" + persno + "]");

        /* ----------------------------------------------------
         * 🔍 DEBUG: What Hibernate REALLY sees
         * ---------------------------------------------------- */
        List<?> raw = entityManager
                .createNativeQuery("SELECT PERSNO FROM ROLESNEW")
                .getResultList();

        System.out.println(">>> RAW ROLESNEW ROW COUNT = " + raw.size());
        raw.forEach(r -> System.out.println(">>> RAW PERSNO = [" + r + "]"));

        String dbName = (String) entityManager
                .createNativeQuery("SELECT sys_context('USERENV','DB_NAME') FROM dual")
                .getSingleResult();

        String schema = (String) entityManager
                .createNativeQuery("SELECT sys_context('USERENV','CURRENT_SCHEMA') FROM dual")
                .getSingleResult();

        System.out.println(">>> CONNECTED DB = " + dbName);
        System.out.println(">>> CONNECTED SCHEMA = " + schema);

        /* ----------------------------------------------------
         * 6️⃣ Fetch roles
         * ---------------------------------------------------- */
        List<RolesNew> roleEntities =
                rolesNewRepository.findByPersno(persno);

        System.out.println(">>> ROLES FOUND COUNT = " + roleEntities.size());
        roleEntities.forEach(r ->
                System.out.println(">>> ROLE = "
                        + r.getRoleName()
                        + " | " + r.getDte()
                        + " | " + r.getDiv())
        );

        if (roleEntities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No roles assigned. Contact administrator.");
        }

        /* 7️⃣ Build response */
        LoginResponse resp = new LoginResponse();
        resp.setEmpId(emp.getEmpId());
        resp.setName(emp.getName());
        resp.setEmail(emp.getEmail());
        resp.setPhone(emp.getPhone());

        resp.setPasswordExpiringSoon(expiringSoon);
        resp.setDaysToPasswordExpiry(daysToExpiry);
        resp.setLastPasswordChangeDate(lastChangeStr);

        /* 8️⃣ Map roles */
        List<UserRoleDTO> roles = roleEntities.stream().map(r -> {
            UserRoleDTO dto = new UserRoleDTO();
            dto.setRoleName(r.getRoleName());
            dto.setRoleNo(r.getRoleNo());
            dto.setDirectorate(r.getDte());
            dto.setDivision(r.getDiv());
            dto.setRoleDesc(r.getRoleDesc());
            return dto;
        }).toList();

        resp.setRoles(roles);

        return ResponseEntity.ok(resp);
    }
}
