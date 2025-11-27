package com.example.attachfile.controller;

import com.example.attachfile.dto.TADTO;
import com.example.attachfile.entity.TAApplication;
import com.example.attachfile.service.TAApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ta")
public class TAApplicationController {

    private final TAApplicationService taService;

    public TAApplicationController(TAApplicationService taService) {
        this.taService = taService;
    }

    // Get all TA applications
    @GetMapping("/all")
    public List<TAApplication> getAllTa() {
        return taService.getAll();
    }

    // Submit new TA
    @PostMapping("/submit")
    public ResponseEntity<?> submitTa(@ModelAttribute TADTO dto) {
        try {
            TAApplication saved = taService.submit(dto);
            return ResponseEntity.ok("TA application submitted successfully with token: " + saved.getApplnNo());
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
    public ResponseEntity<?> getByApplnNo(@PathVariable String ApplnNo) {
        return taService.getByApplnNo(ApplnNo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No TA application found for token: " + ApplnNo));
    }

    // Update existing TA
    @PutMapping("/update/{ApplnNo}")
    public ResponseEntity<?> updateTa(
            @PathVariable String ApplnNo,
            @ModelAttribute TADTO dto
    ) {
        try {
            TAApplication updated = taService.update(ApplnNo, dto);
            return ResponseEntity.ok("TA application updated successfully with token: " + updated.getApplnNo());
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
}
