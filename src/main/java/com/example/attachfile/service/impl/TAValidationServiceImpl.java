package com.example.attachfile.service.impl;

import com.example.attachfile.dto.TADTO;
import com.example.attachfile.entity.TAApplication;
import com.example.attachfile.repository.TAApplicationRepository;
import com.example.attachfile.service.TAValidationService;
import com.example.attachfile.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TAValidationServiceImpl implements TAValidationService {

    private final TAApplicationRepository repository;

    public TAValidationServiceImpl(TAApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateForCreate(TADTO dto, BindingResult bindingResult) {

        // ---------------------------
        // 1) Parse dates using DateUtil
        // ---------------------------
        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate = DateUtil.fromFrontend(dto.getEndDate());
        LocalDate travelDate = DateUtil.fromFrontend(dto.getTravelDate());

        // If null from @NotBlank errors, skip further logic
        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 2) Date relationship checks
        // ---------------------------

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            bindingResult.addError(new FieldError(
                    "TADTO",
                    "startDate",
                    "Start date cannot be after end date"
            ));
        }

        LocalDate today = LocalDate.now();

        if (startDate != null && startDate.isBefore(today.minusYears(1))) {
            bindingResult.addError(new FieldError(
                    "TADTO",
                    "startDate",
                    "Start date is too far in the past"
            ));
        }

        if (endDate != null && endDate.isAfter(today.plusYears(1))) {
            bindingResult.addError(new FieldError(
                    "TADTO",
                    "endDate",
                    "End date is too far in the future"
            ));
        }

        if (travelDate != null && startDate != null && endDate != null) {
            if (travelDate.isBefore(startDate) || travelDate.isAfter(endDate)) {
                bindingResult.addError(new FieldError(
                        "TADTO",
                        "travelDate",
                        "Travel date must be between start date and end date"
                ));
            }
        }

        // ---------------------------
        // 3) Numeric validations
        // ---------------------------
        validatePositiveNumber(dto.getDistance(), "distance", bindingResult);
        validatePositiveNumber(dto.getTaAmount(), "taAmount", bindingResult);

        if (bindingResult.hasErrors()) return;

        // ---------------------------
        // 4) Overlap validation
        // ---------------------------
        if (dto.getEmpId() != null && startDate != null && endDate != null) {
            List<TAApplication> overlaps =
                    repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            dto.getEmpId(),
                            endDate,
                            startDate
                    );

            if (!overlaps.isEmpty()) {
                bindingResult.addError(new FieldError(
                        "TADTO",
                        "startDate",
                        "Another TA application already exists in this date range for this employee"
                ));
            }
        }
    }

    @Override
    public void validateForUpdate(String applnNo, TADTO dto, BindingResult bindingResult) {

        validateForCreate(dto, bindingResult);

        if (bindingResult.hasErrors()) return;

        // Validate application exists
        TAApplication existing = repository.findByApplnNo(applnNo).orElse(null);
        if (existing == null) {
            bindingResult.addError(new FieldError(
                    "TADTO",
                    "applnNo",
                    "No TA application found with token: " + applnNo
            ));
            return;
        }

        LocalDate startDate = DateUtil.fromFrontend(dto.getStartDate());
        LocalDate endDate = DateUtil.fromFrontend(dto.getEndDate());

        if (bindingResult.hasErrors()) return;

        // Overlap check, ignoring itself
        List<TAApplication> overlaps =
                repository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        dto.getEmpId(),
                        endDate,
                        startDate
                );

        boolean hasOtherOverlap = overlaps.stream()
                .anyMatch(app -> !app.getApplnNo().equalsIgnoreCase(applnNo));

        if (hasOtherOverlap) {
            bindingResult.addError(new FieldError(
                    "TADTO",
                    "startDate",
                    "Another TA application exists in this date range for this employee"
            ));
        }
    }

    // -------------------------------------------
    // Helper: validate positive numeric values
    // -------------------------------------------
    private void validatePositiveNumber(String value, String fieldName, BindingResult bindingResult) {
        try {
            BigDecimal number = new BigDecimal(value.trim());
            if (number.compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.addError(new FieldError(
                        "TADTO",
                        fieldName,
                        fieldName + " must be a positive number"
                ));
            }
        } catch (Exception e) {
            bindingResult.addError(new FieldError(
                    "TADTO",
                    fieldName,
                    fieldName + " must be a valid number"
            ));
        }
    }
}
