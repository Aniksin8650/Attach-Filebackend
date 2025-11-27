package com.example.attachfile.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TADTO {

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;
    private String startDate;
    private String endDate;
    private String contact;

    private String applicationType;   // "TA"
    private String applnNo;
    // TA specific fields
    private String travelDate;
    private String distance;
    private String taAmount;
    private String travelMode;

    // Files
    private MultipartFile[] files;
    private String retainedFiles;
}
