package com.example.attachfile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LTCDTO {

    @NotBlank(message = "Employee ID is required")
    private String empId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Start date is required")
    private String startDate;   // yyyy-MM-dd

    @NotBlank(message = "End date is required")
    private String endDate;     // yyyy-MM-dd

    @Pattern(regexp = "\\d{10}", message = "Contact must be 10 digits")
    private String contact;

    @NotBlank(message = "Application type is required")
    private String applicationType;   // "LTC"

    private String applnNo;

    // LTC specific
    @NotBlank(message = "Travel destination is required")
    private String travelDestination;

    @NotBlank(message = "Family members is required")
    private String familyMembers; // validated numeric in validator

    @NotBlank(message = "Claim year is required")
    @Pattern(regexp = "\\d{4}", message = "Claim year must be 4 digits")
    private String claimYear;

    private MultipartFile[] files;
    private String retainedFiles;
}
