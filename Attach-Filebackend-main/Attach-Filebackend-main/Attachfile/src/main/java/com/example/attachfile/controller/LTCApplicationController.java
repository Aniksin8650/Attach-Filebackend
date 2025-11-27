package com.example.attachfile.controller;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
import com.example.attachfile.service.LTCApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ltc")
public class LTCApplicationController {

    private final LTCApplicationService ltcService;

    public LTCApplicationController(LTCApplicationService ltcService) {
        this.ltcService = ltcService;
    }

    @GetMapping("/all")
    public List<LTCApplication> getAllLtc() {
        return ltcService.getAll();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LTCApplication>> getPendingLtc() {
        List<LTCApplication> pending = ltcService.getByStatus("PENDING");
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LTCApplication>> getLtcByStatus(@PathVariable String status) {
        List<LTCApplication> list = ltcService.getByStatus(status.toUpperCase());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitLtc(@ModelAttribute LTCDTO dto) {
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

    @PutMapping("/update/{ApplnNo}")
    public ResponseEntity<?> updateLtc(
            @PathVariable String ApplnNo,
            @ModelAttribute LTCDTO dto
    ) {
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
