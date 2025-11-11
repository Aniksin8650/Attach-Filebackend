package com.example.attachfile.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_applications")
public class LeaveApplication {

    // ✅ Composite key: empId + startDate + endDate
    @EmbeddedId
    private LeaveApplicationId id;

    // Unique token to track/edit applications (e.g., APP-12345678)
    @Column(unique = true)
    private String token;

    private String name;
    private String department;
    private String designation;
    private String reason;
    private String contact;
    private String fileName;          // Semicolon-separated list of uploaded files
    private String applicationType;   // e.g., "leave", "tada", "ltc"

    // Default constructor
    public LeaveApplication() {}

    // ✅ Getters and Setters
    public LeaveApplicationId getId() {
        return id;
    }
    public void setId(LeaveApplicationId id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
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

    // ✅ Convenience getters and setters for embedded ID fields
    public String getEmpId() {
        return id != null ? id.getEmpId() : null;
    }
    public void setEmpId(String empId) {
        if (id == null) id = new LeaveApplicationId();
        id.setEmpId(empId);
    }

    public String getStartDate() {
        return id != null ? id.getStartDate() : null;
    }
    public void setStartDate(String startDate) {
        if (id == null) id = new LeaveApplicationId();
        id.setStartDate(startDate);
    }

    public String getEndDate() {
        return id != null ? id.getEndDate() : null;
    }
    public void setEndDate(String endDate) {
        if (id == null) id = new LeaveApplicationId();
        id.setEndDate(endDate);
    }
}
