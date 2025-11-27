package com.example.attachfile.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LTCDTO {

    private String empId;
    private String name;
    private String department;
    private String designation;

    private String reason;
    private String startDate;
    private String endDate;
    private String contact;

    private String applicationType;   // "LTC"
    private String applnNo;

    // LTC specific
    private String travelDestination;
    private String familyMembers;
    private String claimYear;

    private MultipartFile[] files;
    private String retainedFiles;
}
