package com.example.attachfile.entity;

import com.example.attachfile.dto.TADTO;
import com.example.attachfile.util.DateUtil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TA_APPLICATIONS")
@Getter
@Setter
public class TAApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;

    // DATE in DB
    private LocalDate startDate;
    private LocalDate endDate;

    // NUMBER(10) in DB
    private Long contact;

    private String applicationType;   // "ta"
    private String applnNo;

    // TA specific fields
    private LocalDate travelDate;     // DATE
    private BigDecimal distance;      // NUMBER(10,2)
    private BigDecimal taAmount;      // NUMBER(10,2)
    private String travelMode;

    @Column(length = 2000)
    private String fileName;  // semicolon-separated file names

    @Column(name = "STATUS", length = 20)
    private String status = "PENDING";

    // ============================================
    // Helper: fill entity fields from DTO
    // ============================================
    public void updateFromDTO(TADTO dto, String finalFileNames) {

        this.empId = dto.getEmpId();
        this.name = dto.getName();
        this.department = dto.getDepartment();
        this.designation = dto.getDesignation();

        this.reason = dto.getReason();

        // Dates from "yyyy-MM-dd"
        this.startDate = DateUtil.fromFrontend(dto.getStartDate());
        this.endDate = DateUtil.fromFrontend(dto.getEndDate());

        // Contact from String → Long
        this.contact = parseLong(dto.getContact());

        this.applicationType = dto.getApplicationType() != null
                ? dto.getApplicationType().toLowerCase().trim()
                : null;

        this.applnNo = dto.getApplnNo();

        // TA specific
        this.travelDate = DateUtil.fromFrontend(dto.getTravelDate());
        this.distance   = parseBigDecimal(dto.getDistance());
        this.taAmount   = parseBigDecimal(dto.getTaAmount());
        this.travelMode = dto.getTravelMode();

        this.fileName = finalFileNames;
    }

    // ========= Helper parse methods ==========



    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null; // or throw if you want hard validation
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
