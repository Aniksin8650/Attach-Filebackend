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

    @Value("${uploads.base-dir}")
    private String baseUploadDir;

    public LeaveApplicationController(LeaveApplicationRepository repository) {
        this.repository = repository;
    }

    // ==================== SUBMIT NEW APPLICATION ====================
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
            if (applicationType == null || applicationType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("applicationType is required");
            }
            String typeFolder = applicationType.trim().toLowerCase();

            // ✅ Check for overlapping leaves
            List<LeaveApplication> overlappingLeaves =
                    repository.findOverlappingLeaves(empId, startDate, endDate);
            if (!overlappingLeaves.isEmpty()) {
                LeaveApplication existing = overlappingLeaves.get(0);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Leave dates overlap with an existing application from "
                                + existing.getStartDate() + " to " + existing.getEndDate() + ".");
            }

            // ✅ Prepare directories
            File targetBase = new File(baseUploadDir);
            if (!targetBase.exists() && !targetBase.mkdirs()) {
                targetBase = new File("uploads");
                if (!targetBase.exists()) targetBase.mkdirs();
            }

            File typeDir = new File(targetBase, typeFolder);
            if (!typeDir.exists()) typeDir.mkdirs();

            File empDir = new File(typeDir, empId);
            if (!empDir.exists()) empDir.mkdirs();

            SimpleDateFormat sdf = new SimpleDateFormat("HHmmssddMMyyyy");
            List<String> savedFileNamesList = new ArrayList<>();

            if (files != null) {
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
            }

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
            leave.setFileName(String.join(";", savedFileNamesList));
            leave.setToken(token);

            repository.save(leave);
            return ResponseEntity.ok("Leave application submitted successfully");

        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File save error: " + ioEx.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + ex.getMessage());
        }
    }

    // ==================== GET APPLICATION BY TOKEN ====================
    @GetMapping("/token/{token}")
    public ResponseEntity<?> getApplicationByToken(@PathVariable String token) {
        Optional<LeaveApplication> leaveOpt = repository.findByToken(token);
        if (leaveOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application not found");
        }
        return ResponseEntity.ok(leaveOpt.get());
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
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "retainedFiles", required = false) String retainedFiles
    ) {
        try {
            LeaveApplication existing = repository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Application not found with token: " + token));

            // ✅ Overlap validation
            List<LeaveApplication> overlappingLeaves = repository
                    .findOverlappingLeaves(empId, startDate, endDate)
                    .stream()
                    .filter(l -> !l.getToken().equals(token))
                    .toList();
            if (!overlappingLeaves.isEmpty()) {
                LeaveApplication conflict = overlappingLeaves.get(0);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Updated dates overlap with an existing leave from "
                                + conflict.getStartDate() + " to " + conflict.getEndDate() + ".");
            }

            // ✅ Update basic fields
            existing.setEmpId(empId);
            existing.setName(name);
            existing.setDepartment(department);
            existing.setDesignation(designation);
            existing.setReason(reason);
            existing.setStartDate(startDate);
            existing.setEndDate(endDate);
            existing.setContact(contact);
            existing.setApplicationType(applicationType);

            // ✅ Prepare directories
            File typeDir = new File(baseUploadDir, applicationType.toLowerCase());
            if (!typeDir.exists()) typeDir.mkdirs();
            File empDir = new File(typeDir, empId);
            if (!empDir.exists()) empDir.mkdirs();

         // ✅ Files retained by user (from frontend)
            Set<String> existingFiles = new HashSet<>();
            if (retainedFiles != null && !retainedFiles.isBlank()) {
                existingFiles.addAll(Arrays.asList(retainedFiles.split(";")));
            }

            // ✅ Handle new file uploads (skip duplicates)
            List<String> newFiles = new ArrayList<>();
            if (files != null && files.length > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HHmmssddMMyyyy");
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;

                    String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()))
                            .replaceAll("\\s+", "_")
                            .replaceAll("[^A-Za-z0-9._-]", "");

                    boolean alreadyExists = existingFiles.stream().anyMatch(f -> f.endsWith(original));
                    if (alreadyExists) continue;

                    String timestamp = sdf.format(new Date());
                    String finalName = timestamp + "_" + original;
                    File dest = new File(empDir, finalName);
                    file.transferTo(dest);
//                    System.out.println("✅ Saved file to: " + dest.getAbsolutePath());
                    newFiles.add(finalName);
                }
            }

            // ✅ Merge existing and new files
            existingFiles.addAll(newFiles);

            // ✅ Delete files removed from frontend
            File[] allFiles = empDir.listFiles();
            if (allFiles != null) {
                for (File f : allFiles) {
                    boolean stillReferenced = existingFiles.stream()
                            .anyMatch(saved -> f.getName().equals(saved));
                    if (!stillReferenced) {
                        f.delete(); // remove unused
                    }
                }
            }

            // ✅ Update DB and save
            existing.setFileName(String.join(";", existingFiles));
            repository.save(existing);
            return ResponseEntity.ok("Application updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + ex.getMessage());
        }
    }
}
