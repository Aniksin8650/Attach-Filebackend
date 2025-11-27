package com.example.attachfile.entity;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.util.DateUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "LEAVE_APPLICATIONS")
@Getter @Setter
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;

    // ---- Converted from String → LocalDate ----
    private LocalDate startDate;
    private LocalDate endDate;

    // ---- Converted to NUMBER in Oracle ----
    private Long contact;

    private String applicationType;
    private String applnNo;

    @Column(length = 2000)
    private String fileName;  // semicolon-separated saved file names

    // ============================================
    // Update entity fields from DTO
    // ============================================
    public void updateFromDTO(LeaveDTO dto, String finalFileNames) {

        this.empId = dto.getEmpId();
        this.name = dto.getName();
        this.department = dto.getDepartment();
        this.designation = dto.getDesignation();

        this.reason = dto.getReason();

        // ----- DATE conversion -----
        this.startDate = DateUtil.fromFrontend(dto.getStartDate());
        this.endDate = DateUtil.fromFrontend(dto.getEndDate());

        // ----- CONTACT conversion -----
        this.contact = dto.getContact() == null || dto.getContact().isBlank()
                ? null
                : Long.valueOf(dto.getContact());

        this.applicationType = dto.getApplicationType().toLowerCase().trim();
        this.applnNo = dto.getApplnNo();

        this.fileName = finalFileNames; // merged file names
    }
}
