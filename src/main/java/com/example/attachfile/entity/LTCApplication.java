package com.example.attachfile.entity;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.util.DateUtil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "LTC_APPLICATIONS")
@Getter
@Setter
public class LTCApplication {

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

    private String applicationType;  // "ltc"
    private String applnNo;

    // LTC specific
    private String travelDestination;
    private Integer familyMembers;   // NUMBER(3)
    private Integer claimYear;       // NUMBER(4)

    @Column(length = 2000)
    private String fileName;

    public void updateFromDTO(LTCDTO dto, String finalFileNames) {
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

        this.travelDestination = dto.getTravelDestination();
        this.familyMembers     = parseInteger(dto.getFamilyMembers());
        this.claimYear         = parseInteger(dto.getClaimYear());

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

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
