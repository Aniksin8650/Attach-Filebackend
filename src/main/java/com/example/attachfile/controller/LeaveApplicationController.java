package com.example.attachfile.controller;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.entity.LeaveApplication;
import com.example.attachfile.repository.LeaveApplicationRepository;
import com.example.attachfile.service.LeaveApplicationService;
import com.example.attachfile.service.LeaveValidationService;
import com.example.attachfile.util.ValidationErrorUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leave")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveService;
    private final LeaveValidationService leaveValidationService;
	private final LeaveApplicationRepository repository;

    public LeaveApplicationController(
            LeaveApplicationService leaveService,
            LeaveValidationService leaveValidationService,
            LeaveApplicationRepository repository
    ) {
        this.leaveService = leaveService;
        this.leaveValidationService = leaveValidationService;
        this.repository = repository;
    }

    // ============================
    // GET ALL LEAVE RECORDS
    // ============================
    @GetMapping("/all")
    public List<LeaveApplication> getAllLeaves() {
        return leaveService.getAllLeaves();
    }
    
    @GetMapping("/empId/{empId}")
    public ResponseEntity<List<LeaveApplication>> getLeaveByEmpId(@PathVariable String empId) {
        List<LeaveApplication> list = leaveService.getByEmpId(empId);
        return ResponseEntity.ok(list);
    }
    
    @GetMapping("/count/pending/{empId}")
    public long getPendingLeave(@PathVariable String empId) {
        return repository.countByEmpIdAndStatus(empId, "PENDING");
    }

    // ============================
    // GET ALL PENDING LEAVES (ADMIN)
    // ============================
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveApplication>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    // ============================
    // GET BY STATUS (ADMIN)
    // ============================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveApplication>> getLeavesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(leaveService.getLeavesByStatus(status));
    }

    // ============================
    // SUBMIT NEW LEAVE APPLICATION
    // ============================
    @PostMapping("/submit")
    public ResponseEntity<?> submitLeave(
            @Valid @ModelAttribute LeaveDTO dto,
            BindingResult bindingResult
    ) {
        // module-specific validation (dates, files, overlap)
        leaveValidationService.validateForCreate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            return ValidationErrorUtil.buildErrorResponse(bindingResult, "Validation failed");
        }

        try {
            leaveService.createLeave(dto);
            // Frontend later refetches by ApplnNo anyway
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
        Optional<LeaveApplication> leaveOpt = leaveService.getByApplnNo(ApplnNo);

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
            @Valid @ModelAttribute LeaveDTO dto,
            BindingResult bindingResult
    ) {
        // module-specific validation (dates, files, overlap excluding this appln)
        leaveValidationService.validateForUpdate(ApplnNo, dto, bindingResult);

        if (bindingResult.hasErrors()) {
            return ValidationErrorUtil.buildErrorResponse(bindingResult, "Validation failed");
        }

        try {
            leaveService.updateLeave(ApplnNo, dto);
            return ResponseEntity.ok("Leave application updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    // ===============================
    // UPDATE STATUS (ADMIN)
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

        try {
            LeaveApplication updated = leaveService.updateStatus(ApplnNo, statusStr);
            return ResponseEntity.ok("Status updated to " + updated.getStatus());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}
