package com.example.attachfile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LeaveDTO {

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
    private String startDate;  // yyyy-MM-dd from frontend

    @NotBlank(message = "End date is required")
    private String endDate;    // yyyy-MM-dd from frontend

    @Pattern(regexp = "\\d{10}", message = "Contact must be 10 digits")
    private String contact;

    @NotBlank(message = "Application type is required")
    private String applicationType;

    private String applnNo;        // token from frontend (APP-...)

    // for update – semicolon separated list of kept files
    private String retainedFiles;

    // ✅ MUST be array to match FileStorageService
    private MultipartFile[] files;
}
