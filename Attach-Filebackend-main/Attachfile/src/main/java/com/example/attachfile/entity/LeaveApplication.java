package com.example.attachfile.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_applications")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private String name;
    private String department;
    private String designation;
    private String reason;
    private String startDate;  // or LocalDate if you prefer
    private String endDate;    // or LocalDate
    private String contact;
    private String fileName;   // Timestamped filenames separated by ;
    private String applicationType; // "leave", "tada", "ltc"

    // Default constructor
    public LeaveApplication() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getEmpId() {
        return empId;
    }
    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getApplicationType() {
        return applicationType;
    }
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }
}
