package com.example.attachfile.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LeaveDTO {

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;
    private String startDate;
    private String endDate;
    private String contact;

    private String applicationType;
    private String applnNo;

    private MultipartFile[] files;
    private String retainedFiles;
}
