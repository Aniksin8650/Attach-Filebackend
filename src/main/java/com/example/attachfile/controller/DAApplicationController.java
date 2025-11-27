package com.example.attachfile.controller;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;
import com.example.attachfile.service.DAApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/da")
public class DAApplicationController {

    private final DAApplicationService daService;

    public DAApplicationController(DAApplicationService daService) {
        this.daService = daService;
    }

    // Get all DA applications
    @GetMapping("/all")
    public List<DAApplication> getAllDa() {
        return daService.getAll();
    }

    // 🆕 Get all PENDING DA applications
    @GetMapping("/pending")
    public ResponseEntity<List<DAApplication>> getPendingDa() {
        List<DAApplication> pending = daService.getByStatus("PENDING");
        return ResponseEntity.ok(pending);
    }

    // 🆕 Get DA applications by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DAApplication>> getDaByStatus(@PathVariable String status) {
        List<DAApplication> list = daService.getByStatus(status.toUpperCase());
        return ResponseEntity.ok(list);
    }

    // Submit new DA
    @PostMapping("/submit")
    public ResponseEntity<?> submitDa(@ModelAttribute DADTO dto) {
        try {
            DAApplication saved = daService.submit(dto);
            return ResponseEntity.ok("DA application submitted successfully with token: " + saved.getApplnNo());
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

    // Get by token
    @GetMapping("/ApplnNo/{ApplnNo}")
    public ResponseEntity<?> getDaByApplnNo(@PathVariable String ApplnNo) {
        return daService.getByApplnNo(ApplnNo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No DA application found for token: " + ApplnNo));
    }

    // Update existing DA
    @PutMapping("/update/{ApplnNo}")
    public ResponseEntity<?> updateDa(
            @PathVariable String ApplnNo,
            @ModelAttribute DADTO dto
    ) {
        try {
            DAApplication updated = daService.update(ApplnNo, dto);
            return ResponseEntity.ok("DA application updated successfully with token: " + updated.getApplnNo());
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

    // 🆕 Update status (Admin)
    @PutMapping("/status/{ApplnNo}")
    public ResponseEntity<?> updateDaStatus(
            @PathVariable String ApplnNo,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'status' in request body");
        }

        String normalizedStatus = statusStr.toUpperCase();
        try {
            DAApplication updated = daService.updateStatus(ApplnNo, normalizedStatus);
            return ResponseEntity.ok("DA status updated to " + updated.getStatus());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Status update failed: " + ex.getMessage());
        }
    }
}
