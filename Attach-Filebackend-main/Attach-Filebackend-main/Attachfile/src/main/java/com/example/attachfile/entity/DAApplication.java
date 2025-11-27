package com.example.attachfile.entity;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.util.DateUtil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "DA_APPLICATIONS")
@Getter
@Setter
public class DAApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long contact;

    private String applicationType;   // "da"
    private String applnNo;

    // DA specific
    private LocalDate billDate;
    private BigDecimal billAmount;
    private String purpose;

    @Column(length = 2000)
    private String fileName;
    
    @Column(name = "STATUS", length = 20)
    private String status = "PENDING";


    public void updateFromDTO(DADTO dto, String finalFileNames) {
        this.empId = dto.getEmpId();
        this.name = dto.getName();
        this.department = dto.getDepartment();
        this.designation = dto.getDesignation();

        this.reason = dto.getReason();

        this.startDate = DateUtil.fromFrontend(dto.getStartDate());
        this.endDate = DateUtil.fromFrontend(dto.getEndDate());
        this.contact   = parseLong(dto.getContact());

        this.applicationType = dto.getApplicationType() != null
                ? dto.getApplicationType().toLowerCase().trim()
                : null;

        this.applnNo = dto.getApplnNo();

        this.billDate   = DateUtil.fromFrontend(dto.getBillDate());
        this.billAmount = parseBigDecimal(dto.getBillAmount());
        this.purpose    = dto.getPurpose();

        this.fileName = finalFileNames;
    }



    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
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
