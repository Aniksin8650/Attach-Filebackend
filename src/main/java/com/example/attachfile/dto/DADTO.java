package com.example.attachfile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DADTO {

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
    private String startDate;  // yyyy-MM-dd

    @NotBlank(message = "End date is required")
    private String endDate;    // yyyy-MM-dd

    @Pattern(regexp = "\\d{10}", message = "Contact must be 10 digits")
    private String contact;

    @NotBlank(message = "Application type is required")
    private String applicationType;   // "DA"

    private String applnNo;

    // DA specific
    @NotBlank(message = "Bill date is required")
    private String billDate;   // yyyy-MM-dd

    @NotBlank(message = "Bill amount is required")
    private String billAmount; // validated as positive number in validator

    @NotBlank(message = "Purpose is required")
    private String purpose;

    // Files
    private MultipartFile[] files;
    private String retainedFiles;
}
