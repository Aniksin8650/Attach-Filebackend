package com.example.attachfile.controller;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;
import com.example.attachfile.service.DAApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/da")
public class DAApplicationController {

    private final DAApplicationService daService;

    public DAApplicationController(DAApplicationService daService) {
        this.daService = daService;
    }

    @GetMapping("/all")
    public List<DAApplication> getAllDa() {
        return daService.getAll();
    }

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

    @GetMapping("/ApplnNo/{ApplnNo}")
    public ResponseEntity<?> getByApplnNo(@PathVariable String ApplnNo) {
        return daService.getByApplnNo(ApplnNo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No DA application found for token: " + ApplnNo));
    }

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
}
