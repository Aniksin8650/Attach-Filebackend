package com.example.attachfile.controller;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
import com.example.attachfile.service.LTCApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<?> getByToken(@PathVariable String ApplnNo) {
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
}
