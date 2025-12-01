package com.example.attachfile.service;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.entity.LeaveApplication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface LeaveApplicationService {

    List<LeaveApplication> getAllLeaves();

    List<LeaveApplication> getPendingLeaves();

    List<LeaveApplication> getLeavesByStatus(String status);

    Optional<LeaveApplication> getByApplnNo(String applnNo);

    LeaveApplication createLeave(LeaveDTO dto) throws IOException;

    LeaveApplication updateLeave(String applnNo, LeaveDTO dto) throws IOException;

    LeaveApplication updateStatus(String applnNo, String status);
    
    List<LeaveApplication> getByEmpId(String empId);
}
