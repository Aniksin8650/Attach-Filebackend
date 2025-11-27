package com.example.attachfile.controller;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.entity.LeaveApplication;
import com.example.attachfile.repository.LeaveApplicationRepository;
import com.example.attachfile.service.FileStorageService;
import com.example.attachfile.util.DateUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/leave")
public class LeaveApplicationController {

    private final LeaveApplicationRepository repository;
    private final FileStorageService fileStorageService;

    public LeaveApplicationController(
            LeaveApplicationRepository repository,
            FileStorageService fileStorageService
    ) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    // ============================
    // GET ALL LEAVE RECORDS
    // ============================
    @GetMapping("/all")
    public List<LeaveApplication> getAllLeaves() {
        return repository.findAll();
    }

    // ============================
    // 🆕 GET ALL PENDING LEAVES (ADMIN)
    // /api/leave/pending
    // ============================
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveApplication>> getPendingLeaves() {
        List<LeaveApplication> pending = repository.findByStatus("PENDING");
        return ResponseEntity.ok(pending);
    }

    // ============================
    // 🆕 GET BY STATUS (ADMIN - generic)
    // /api/leave/status/APPROVED, /PENDING, /REJECTED...
    // ============================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveApplication>> getLeavesByStatus(@PathVariable String status) {
        List<LeaveApplication> list = repository.findByStatus(status.toUpperCase());
        return ResponseEntity.ok(list);
    }

    // ============================
    // SUBMIT NEW LEAVE APPLICATION
    // ============================
    @PostMapping("/submit")
    public ResponseEntity<?> submitLeave(@ModelAttribute LeaveDTO dto) {
        try {
            LocalDate start = DateUtil.fromFrontend(dto.getStartDate()); // expects "yyyy-MM-dd"
            LocalDate end   = DateUtil.fromFrontend(dto.getEndDate());

            // -------------------------------
            // Overlap Check
            // -------------------------------
            List<LeaveApplication> overlaps =
                    repository.findOverlappingLeaves(dto.getEmpId(), start , end);

            if (!overlaps.isEmpty()) {
                LeaveApplication existing = overlaps.get(0);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Leave dates overlap with an existing leave from "
                                + existing.getStartDate() + " to " + existing.getEndDate());
            }

            // -------------------------------
            // Create Directory
            // -------------------------------
            File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

            // -------------------------------
            // Save new files
            // -------------------------------
            List<String> savedFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);

            // -------------------------------
            // Create Entity from DTO
            // -------------------------------
            LeaveApplication leave = new LeaveApplication();
            leave.updateFromDTO(dto, String.join(";", savedFiles));

            // 🆕 ensure status is PENDING for new applications
            leave.setStatus("PENDING");

            repository.save(leave);

            return ResponseEntity.ok("Leave application submitted successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File save failed: " + e.getMessage());
        }
    }

    // ===============================
    // GET BY TOKEN
    // ===============================
    @GetMapping("/ApplnNo/{ApplnNo}")
    public ResponseEntity<?> getLeaveByToken(@PathVariable String ApplnNo) {

        Optional<LeaveApplication> leaveOpt = repository.findByApplnNo(ApplnNo);

        if (leaveOpt.isPresent()) {
            return ResponseEntity.ok(leaveOpt.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No leave application found for token: " + ApplnNo);
    }

    // ===============================
    // UPDATE EXISTING LEAVE
    // ===============================
    @PutMapping("/update/{ApplnNo}")
    public ResponseEntity<?> updateLeave(
            @PathVariable String ApplnNo,
            @ModelAttribute LeaveDTO dto
    ) {
        try {
            LeaveApplication existing = repository.findByApplnNo(ApplnNo)
                    .orElseThrow(() -> new RuntimeException("No application found with token: " + ApplnNo));
            
            LocalDate start = DateUtil.fromFrontend(dto.getStartDate());
            LocalDate end   = DateUtil.fromFrontend(dto.getEndDate());

            // -------------------------------
            // Overlap check for other leaves
            // -------------------------------
            List<LeaveApplication> overlaps =
                    repository.findOverlappingLeaves(dto.getEmpId(), start, end)
                            .stream()
                            .filter(l -> !l.getApplnNo().equals(ApplnNo))
                            .toList();

            if (!overlaps.isEmpty()) {
                LeaveApplication conflict = overlaps.get(0);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Updated dates overlap with an existing leave from "
                                + conflict.getStartDate() + " to " + conflict.getEndDate());
            }

            // -------------------------------
            // Prepare directory
            // -------------------------------
            File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

            // -------------------------------
            // Parse retained file names
            // -------------------------------
            Set<String> retained = fileStorageService.parseRetainedFiles(dto.getRetainedFiles());

            // -------------------------------
            // Save NEW files only
            // -------------------------------
            List<String> newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);

            // -------------------------------
            // Delete removed files
            // -------------------------------
            fileStorageService.deleteRemovedFiles(retained, empDir);

            // -------------------------------
            // Merge filenames
            // -------------------------------
            String finalFileNames = fileStorageService.mergeFileNames(retained, newFiles);

            // -------------------------------
            // Update entity (but not status)
            // -------------------------------
            existing.updateFromDTO(dto, finalFileNames);

            // 🟡 Optional rule:
            // If you want edited applications to go back to PENDING:
            // existing.setStatus("PENDING");

            repository.save(existing);

            return ResponseEntity.ok("Leave application updated successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + ex.getMessage());
        }
    }

    // ===============================
    // 🆕 UPDATE STATUS (ADMIN)
    // e.g. PUT /api/leave/status/LA-2025-001
    // body: { "status": "APPROVED" }
    // ===============================
    @PutMapping("/status/{ApplnNo}")
    public ResponseEntity<?> updateStatus(
            @PathVariable String ApplnNo,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'status' in request body");
        }

        // Normalize to uppercase for consistency in DB
        String normalizedStatus = statusStr.toUpperCase();

        LeaveApplication existing = repository.findByApplnNo(ApplnNo)
                .orElseThrow(() -> new RuntimeException("No application found with token: " + ApplnNo));

        existing.setStatus(normalizedStatus);
        repository.save(existing);

        return ResponseEntity.ok("Status updated to " + normalizedStatus);
    }
}
