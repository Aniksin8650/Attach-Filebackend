package com.example.attachfile.service.impl;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;
import com.example.attachfile.repository.DAApplicationRepository;
import com.example.attachfile.service.DAValidationService;
import com.example.attachfile.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DAValidationServiceImpl implements DAValidationService {

    private final DAApplicationRepository repository;

    public DAValidationServiceImpl(DAApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateForCreate(DADTO dto, BindingResult bindingResult) {

        // ---------------------------
        // 1) Parse dates via DateUtil
        // ---------------------------
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate   = DateUtil.fromFrontend(dto.getEndDate());
        LocalDate billDate  = DateUtil.fromFrontend(dto.getBillDate());

        // If JSR-303 (@NotBlank etc.) has already added errors, stop early
        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 2) Date relationship checks
        // ---------------------------

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            bindingResult.addError(new FieldError(
                    "DADTO",
                    "startDate",
                    "Start date cannot be after end date"
            ));
        }

        LocalDate today = LocalDate.now();

        if (startDate != null && startDate.isBefore(today.minusYears(1))) {
            bindingResult.addError(new FieldError(
                    "DADTO",
                    "startDate",
                    "Start date is too far in the past"
            ));
        }

        if (endDate != null && endDate.isAfter(today.plusYears(1))) {
            bindingResult.addError(new FieldError(
                    "DADTO",
                    "endDate",
                    "End date is too far in the future"
            ));
        }

        // Bill date within leave period (optional but nice)
        if (billDate != null && startDate != null && endDate != null) {
            if (billDate.isBefore(startDate) || billDate.isAfter(endDate)) {
                bindingResult.addError(new FieldError(
                        "DADTO",
                        "billDate",
                        "Bill date must be between start date and end date"
                ));
            }
        }

        // ---------------------------
        // 3) Bill amount numeric + positive
        // ---------------------------
        if (dto.getBillAmount() != null && !dto.getBillAmount().isBlank()) {
            try {
                BigDecimal amount = new BigDecimal(dto.getBillAmount().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    bindingResult.addError(new FieldError(
                            "DADTO",
                            "billAmount",
                            "Bill amount must be a positive number"
                    ));
                }
            } catch (NumberFormatException ex) {
                bindingResult.addError(new FieldError(
                        "DADTO",
                        "billAmount",
                        "Bill amount must be a valid number"
                ));
            }
        }

        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 4) Overlap validation
        // ---------------------------
        if (dto.getEmpId() != null && startDate != null && endDate != null) {
            List<DAApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            if (!overlaps.isEmpty()) {
                bindingResult.addError(new FieldError(
                        "DADTO",
                        "startDate",
                        "There is already a DA application in this date range for this employee"
                ));
            }
        }
    }

    @Override
    public void validateForUpdate(String applnNo, DADTO dto, BindingResult bindingResult) {

        // 1) Run all create validations first
        validateForCreate(dto, bindingResult);

        if (bindingResult.hasErrors()) return;

        // 2) Ensure application exists
        DAApplication existing = repository.findByApplnNo(applnNo).orElse(null);
        if (existing == null) {
            bindingResult.addError(new FieldError(
                    "DADTO",
                    "applnNo",
                    "No DA application found with token: " + applnNo
            ));
            return;
        }

        // 3) Overlap check again but ignore self
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate   = DateUtil.fromFrontend(dto.getEndDate());

        if (startDate != null && endDate != null && dto.getEmpId() != null) {
            List<DAApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            boolean hasOtherOverlap = overlaps.stream()
                    .anyMatch(app -> !app.getApplnNo().equalsIgnoreCase(applnNo));

            if (hasOtherOverlap) {
                bindingResult.addError(new FieldError(
                        "DADTO",
                        "startDate",
                        "Another DA application exists in this date range for this employee"
                ));
            }
        }

        // Optional: status-based restrictions similar to Leave / TA
        // if ("APPROVED".equalsIgnoreCase(existing.getStatus())) { ... }
    }
}
