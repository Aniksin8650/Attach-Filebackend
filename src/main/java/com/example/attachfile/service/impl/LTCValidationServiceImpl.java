package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
import com.example.attachfile.repository.LTCApplicationRepository;
import com.example.attachfile.service.LTCValidationService;
import com.example.attachfile.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.util.List;

@Service
public class LTCValidationServiceImpl implements LTCValidationService {

    private final LTCApplicationRepository repository;

    public LTCValidationServiceImpl(LTCApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateForCreate(LTCDTO dto, BindingResult bindingResult) {

        // ---------------------------
        // 1) Parse dates via DateUtil
        // ---------------------------
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate   = DateUtil.fromFrontend(dto.getEndDate());

        // If Bean Validation already added errors (@NotBlank etc.) stop early
        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 2) Date relationship checks
        // ---------------------------
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            bindingResult.addError(new FieldError(
                    "LTCDTO",
                    "startDate",
                    "Start date cannot be after end date"
            ));
        }

        LocalDate today = LocalDate.now();

        if (startDate != null && startDate.isBefore(today.minusYears(1))) {
            bindingResult.addError(new FieldError(
                    "LTCDTO",
                    "startDate",
                    "Start date is too far in the past"
            ));
        }

        if (endDate != null && endDate.isAfter(today.plusYears(1))) {
            bindingResult.addError(new FieldError(
                    "LTCDTO",
                    "endDate",
                    "End date is too far in the future"
            ));
        }

        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 3) Overlap validation (same style as Leave / DA / TA)
        // ---------------------------
        if (dto.getEmpId() != null && startDate != null && endDate != null) {
            List<LTCApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            if (!overlaps.isEmpty()) {
                bindingResult.addError(new FieldError(
                        "LTCDTO",
                        "startDate",
                        "There is already an LTC application in this date range for this employee"
                ));
            }
        }
    }

    @Override
    public void validateForUpdate(String applnNo, LTCDTO dto, BindingResult bindingResult) {

        // 1) Run create validations first (dates + overlap)
        validateForCreate(dto, bindingResult);

        if (bindingResult.hasErrors()) return;

        // 2) Ensure application exists
        LTCApplication existing = repository.findByApplnNo(applnNo).orElse(null);
        if (existing == null) {
            bindingResult.addError(new FieldError(
                    "LTCDTO",
                    "applnNo",
                    "No LTC application found with token: " + applnNo
            ));
            return;
        }

        // 3) Overlap check again, ignoring self
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate   = DateUtil.fromFrontend(dto.getEndDate());

        if (startDate != null && endDate != null && dto.getEmpId() != null) {
            List<LTCApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            boolean hasOtherOverlap = overlaps.stream()
                    .anyMatch(app -> !app.getApplnNo().equalsIgnoreCase(applnNo));

            if (hasOtherOverlap) {
                bindingResult.addError(new FieldError(
                        "LTCDTO",
                        "startDate",
                        "Another LTC application exists in this date range for this employee"
                ));
            }
        }

        // Optional: lock edits based on status
        // if ("APPROVED".equalsIgnoreCase(existing.getStatus())) { ... }
    }
}
