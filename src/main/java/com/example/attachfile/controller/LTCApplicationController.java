package com.example.attachfile.controller;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
import com.example.attachfile.repository.LTCApplicationRepository;
import com.example.attachfile.service.LTCApplicationService;
import com.example.attachfile.service.LTCValidationService;
import com.example.attachfile.util.ValidationErrorUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ltc")
public class LTCApplicationController {

    private final LTCApplicationService ltcService;
    private final LTCValidationService ltcValidationService;
	private final LTCApplicationRepository repository;

    public LTCApplicationController(LTCApplicationService ltcService,
                                    LTCValidationService ltcValidationService,
                                    LTCApplicationRepository repository) {
        this.ltcService = ltcService;
        this.ltcValidationService = ltcValidationService;
        this.repository = repository;
    }

    @GetMapping("/all")
    public List<LTCApplication> getAllLtc() {
        return ltcService.getAll();
    }
    
    @GetMapping("/count/pending/{empId}")
    public long getPendingLtc(@PathVariable String empId) {
		return repository.countByEmpIdAndStatus(empId, "PENDING");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LTCApplication>> getPendingLtc() {
        List<LTCApplication> pending = ltcService.getByStatus("PENDING");
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/empId/{empId}")
    public ResponseEntity<List<LTCApplication>> getLeaveByEmpId(@PathVariable String empId) {
        List<LTCApplication> list = ltcService.getByEmpId(empId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LTCApplication>> getLtcByStatus(@PathVariable String status) {
        List<LTCApplication> list = ltcService.getByStatus(status.toUpperCase());
        return ResponseEntity.ok(list);
    }

    // ===============================
    // Submit new LTC (WITH validation)
    // ===============================
    @PostMapping("/submit")
    public ResponseEntity<?> submitLtc(
            @Valid @ModelAttribute LTCDTO dto,
            BindingResult bindingResult
    ) {
        // 1) Run backend/module-specific validation
        ltcValidationService.validateForCreate(dto, bindingResult);

        // 2) If errors → DO NOT call service, DO NOT save files
        if (bindingResult.hasErrors()) {
            return ValidationErrorUtil.buildErrorResponse(bindingResult, "Validation failed");
        }

        // 3) Only now save files + DB
        try {
            LTCApplication saved = ltcService.submit(dto);
            return ResponseEntity.ok("LTC application submitted successfully with token: " + saved.getApplnNo());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File save failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Submit failed: " + e.getMessage());
        }
    }

    @GetMapping("/ApplnNo/{ApplnNo}")
    public ResponseEntity<?> getLtcByApplnNo(@PathVariable String ApplnNo) {
        return ltcService.getByApplnNo(ApplnNo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No LTC application found for token: " + ApplnNo));
    }

    // ===============================
    // Update existing LTC (WITH validation)
    // ===============================
    @PutMapping("/update/{ApplnNo}")
    public ResponseEntity<?> updateLtc(
            @PathVariable String ApplnNo,
            @Valid @ModelAttribute LTCDTO dto,
            BindingResult bindingResult
    ) {
        // 1) Run backend validation for update
        ltcValidationService.validateForUpdate(ApplnNo, dto, bindingResult);

        // 2) If errors → DO NOT touch files
        if (bindingResult.hasErrors()) {
            return ValidationErrorUtil.buildErrorResponse(bindingResult, "Validation failed");
        }

        // 3) Only now perform file operations + DB update
        try {
            LTCApplication updated = ltcService.update(ApplnNo, dto);
            return ResponseEntity.ok("LTC application updated successfully with token: " + updated.getApplnNo());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File update failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }

    @PutMapping("/status/{ApplnNo}")
    public ResponseEntity<?> updateLtcStatus(
            @PathVariable String ApplnNo,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'status' in request body");
        }

        String normalizedStatus = statusStr.toUpperCase();
        try {
            LTCApplication updated = ltcService.updateStatus(ApplnNo, normalizedStatus);
            return ResponseEntity.ok("LTC status updated to " + updated.getStatus());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Status update failed: " + ex.getMessage());
        }
    }
}
