package com.example.attachfile.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DADTO {

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;
    private String startDate;
    private String endDate;
    private String contact;

    private String applicationType;   // "DA"
    private String applnNo;
    // DA specific
    private String billDate;
    private String billAmount;
    private String purpose;

    private MultipartFile[] files;
    private String retainedFiles;
}
