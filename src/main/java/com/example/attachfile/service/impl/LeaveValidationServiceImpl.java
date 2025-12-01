package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.entity.LeaveApplication;
import com.example.attachfile.repository.LeaveApplicationRepository;
import com.example.attachfile.service.LeaveValidationService;
import com.example.attachfile.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveValidationServiceImpl implements LeaveValidationService {

    private final LeaveApplicationRepository repository;

    public LeaveValidationServiceImpl(LeaveApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateForCreate(LeaveDTO dto, BindingResult bindingResult) {

        // ---------------------------
        // 1) Parse dates from frontend strings using DateUtil
        // ---------------------------
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate   = DateUtil.fromFrontend(dto.getEndDate());

        // If JSR-303 (@NotBlank, etc.) has already put errors, don't go further
        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 2) Date relationship checks
        // ---------------------------
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            bindingResult.addError(new FieldError(
                    "LeaveDTO",
                    "startDate",
                    "Start date cannot be after end date"
            ));
        }

        LocalDate today = LocalDate.now();

        // Example limits: adjust as per your business rules
        if (startDate != null && startDate.isBefore(today.minusYears(1))) {
            bindingResult.addError(new FieldError(
                    "LeaveDTO",
                    "startDate",
                    "Start date is too far in the past"
            ));
        }

        if (endDate != null && endDate.isAfter(today.plusYears(1))) {
            bindingResult.addError(new FieldError(
                    "LeaveDTO",
                    "endDate",
                    "End date is too far in the future"
            ));
        }

        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 3) Overlap validation (no overlapping leave for same empId)
        // ---------------------------
        if (dto.getEmpId() != null && startDate != null && endDate != null) {
            List<LeaveApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            if (!overlaps.isEmpty()) {
                bindingResult.addError(new FieldError(
                        "LeaveDTO",
                        "startDate",
                        "There is already a leave application in this date range for this employee"
                ));
            }
        }
    }

    @Override
    public void validateForUpdate(String applnNo, LeaveDTO dto, BindingResult bindingResult) {

        // 1) Run all create validations first
        validateForCreate(dto, bindingResult);

        if (bindingResult.hasErrors()) return;

        // 2) Ensure application exists
        LeaveApplication existing = repository.findByApplnNo(applnNo).orElse(null);
        if (existing == null) {
            bindingResult.addError(new FieldError(
                    "LeaveDTO",
                    "applnNo",
                    "No leave application found with token: " + applnNo
            ));
            return;
        }

        // 3) Overlap check again but ignore self
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate   = DateUtil.fromFrontend(dto.getEndDate());

        if (startDate != null && endDate != null && dto.getEmpId() != null) {
            List<LeaveApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            boolean hasOtherOverlap = overlaps.stream()
                    .anyMatch(app -> !app.getApplnNo().equalsIgnoreCase(applnNo));

            if (hasOtherOverlap) {
                bindingResult.addError(new FieldError(
                        "LeaveDTO",
                        "startDate",
                        "Another leave application exists in this date range for this employee"
                ));
            }
        }

        // Optional: status-based restrictions
        // if ("APPROVED".equalsIgnoreCase(existing.getStatus())) { ... }
    }
}
