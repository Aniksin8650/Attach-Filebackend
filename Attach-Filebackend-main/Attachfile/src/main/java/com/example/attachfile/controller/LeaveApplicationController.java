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
            File typeDir = new File(targetBase, typeFolder);
            if (!typeDir.exists()) typeDir.mkdirs();

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
                    String finalName = timestamp + original;

                    // Ensure uniqueness by appending a short random suffix if file exists
                    File dest = new File(typeDir, finalName);
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
}
