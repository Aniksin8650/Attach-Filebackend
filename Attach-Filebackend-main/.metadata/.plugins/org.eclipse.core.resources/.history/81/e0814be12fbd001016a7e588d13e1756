package com.example.attachfile.controller;

import com.example.attachfile.entity.LeaveApplication;
import com.example.attachfile.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/leave")
public class LeaveApplicationController {

    private final LeaveApplicationRepository repository;

    // base upload directory from application.properties (example: uploads.base-dir=C:/LeaveApplications)
    @Value("${uploads.base-dir}")
    private String baseUploadDir;

    public LeaveApplicationController(LeaveApplicationRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitLeaveApplication(
            @RequestParam("empId") String empId,
            @RequestParam("name") String name,
            @RequestParam("department") String department,
            @RequestParam("designation") String designation,
            @RequestParam("reason") String reason,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("token") String token,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam("applicationType") String applicationType,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {

        try {
            // Validate applicationType and normalise
            if (applicationType == null || applicationType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("applicationType is required");
            }
            String typeFolder = applicationType.trim().toLowerCase();

            // Resolve target folder and make sure it exists
            File targetBase = new File(baseUploadDir);
            if (!targetBase.exists()) {
                if (!targetBase.mkdirs()) {
                    // fallback to relative folder if creation fails
                    targetBase = new File("uploads");
                    if (!targetBase.exists()) targetBase.mkdirs();
                }
            }
         // Create applicationType directory (e.g., leave, tada)
            File typeDir = new File(targetBase, typeFolder);
            if (!typeDir.exists()) typeDir.mkdirs();

            // ✅ Create employee-specific sub folder inside type folder
            File empDir = new File(typeDir, empId);
            if (!empDir.exists()) empDir.mkdirs();


            // Prepare time stamp format HHmmssddMMyyyy
            SimpleDateFormat sdf = new SimpleDateFormat("HHmmssddMMyyyy");
            List<String> savedFileNamesList = new ArrayList<>();

            if (files != null) {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;

                    String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                    // Sanitise filename - remove path sequences and forbid suspicious chars
                    original = original.replaceAll("\\s+", "_"); // replace spaces
                    original = original.replaceAll("[^A-Za-z0-9._-]", ""); // allow only letters, numbers, dot, underscore, dash

                    // Generate time stamp prefix
                    String timestamp = sdf.format(new Date());

                    // Compose final filename
                    String finalName = timestamp +"_"+ original;

                    // Ensure uniqueness by appending a short random suffix if file exists
                    File dest = new File(empDir, finalName);
                    if (dest.exists()) {
                        String baseName = finalName;
                        String suffix = "-" + UUID.randomUUID().toString().substring(0, 6);
                        dest = new File(typeDir, baseName + suffix);
                        finalName = baseName + suffix;
                    }

                    // Save physical file
                    file.transferTo(dest);

                    savedFileNamesList.add(finalName);
                }
            }

            // Build the DB entity and save
            LeaveApplication leave = new LeaveApplication();
            leave.setEmpId(empId);
            leave.setName(name);
            leave.setDepartment(department);
            leave.setDesignation(designation);
            leave.setReason(reason);
            leave.setStartDate(startDate);
            leave.setEndDate(endDate);
            leave.setContact(contact);
            leave.setApplicationType(typeFolder);
            // join filenames with semicolon
            String joined = String.join(";", savedFileNamesList);
            leave.setFileName(joined);
            leave.setToken(token);


            repository.save(leave);

            return ResponseEntity.ok("Leave application submitted successfully");
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File save error: " + ioEx.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + ex.getMessage());
        }
    }
    @GetMapping("/token/{token}")
    public ResponseEntity<?> getApplicationByToken(@PathVariable String token) {
        Optional<LeaveApplication> leaveOpt = repository.findByToken(token);
        if (leaveOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application not found");
        }
        LeaveApplication leave = leaveOpt.get(); // ✅ extract the object
        return ResponseEntity.ok(leave);
    }
 // ==================== UPDATE EXISTING APPLICATION ====================
    @PutMapping("/update/{token}")
    public ResponseEntity<?> updateLeaveApplicationByToken(
            @PathVariable String token,
            @RequestParam("empId") String empId,
            @RequestParam("name") String name,
            @RequestParam("department") String department,
            @RequestParam("designation") String designation,
            @RequestParam("reason") String reason,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam("applicationType") String applicationType,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        try {
            // 🔍 find by token instead of ID
            LeaveApplication existing = repository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Application not found with token: " + token));

            // ✅ then continue with your existing update logic below...


            // Update basic fields
            existing.setEmpId(empId);
            existing.setName(name);
            existing.setDepartment(department);
            existing.setDesignation(designation);
            existing.setReason(reason);
            existing.setStartDate(startDate);
            existing.setEndDate(endDate);
            existing.setContact(contact);
            existing.setApplicationType(applicationType);

            // Handle new file uploads (optional)
            if (files != null && files.length > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HHmmssddMMyyyy");
                List<String> savedFileNamesList = new ArrayList<>();

                File typeDir = new File(baseUploadDir, applicationType.toLowerCase());
                if (!typeDir.exists()) typeDir.mkdirs();

                // ✅ Create employee-specific sub folder for updates too
                File empDir = new File(typeDir, empId);
                if (!empDir.exists()) empDir.mkdirs();

                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;

                    String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()))
                            .replaceAll("\\s+", "_")
                    		.replaceAll("[^A-Za-z0-9._-]", "");
                    String timestamp = sdf.format(new Date());
                    String finalName = timestamp + "_" + original;

                    File dest = new File(empDir, finalName);
                    file.transferTo(dest);

                    savedFileNamesList.add(finalName);
                }

                existing.setFileName(String.join(";", savedFileNamesList));
            }

            repository.save(existing);
            return ResponseEntity.ok("Application updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + ex.getMessage());
        }
    }
}
