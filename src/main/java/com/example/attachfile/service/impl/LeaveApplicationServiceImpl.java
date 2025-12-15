package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.entity.LeaveApplication;
import com.example.attachfile.exception.MaxPendingApplicationsException;
import com.example.attachfile.repository.LeaveApplicationRepository;
import com.example.attachfile.service.FileStorageService;
import com.example.attachfile.service.LeaveApplicationService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private final LeaveApplicationRepository repository;
    private final FileStorageService fileStorageService;

    private static final int MAX_PENDING = 3;
    private static final Set<String> PENDING_STATUSES =
            Set.of("PENDING", "FORWARDED", "SUBMITTED");

    public LeaveApplicationServiceImpl(
            LeaveApplicationRepository repository,
            FileStorageService fileStorageService
    ) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<LeaveApplication> getAllLeaves() {
        return repository.findAll();
    }

    @Override
    public List<LeaveApplication> getByEmpId(String empId) {
        return repository.findByEmpId(empId);
    }

    @Override
    public List<LeaveApplication> getPendingLeaves() {
        return repository.findByStatus("PENDING");
    }

    @Override
    public List<LeaveApplication> getLeavesByStatus(String status) {
        return repository.findByStatus(status.toUpperCase());
    }

    @Override
    public Optional<LeaveApplication> getByApplnNo(String applnNo) {
        return repository.findByApplnNo(applnNo);
    }

    /**
     * CREATE new leave application
     * ❌ Block if MAX_PENDING reached
     * ✅ Edit/update still allowed via updateLeave(...)
     */
    @Override
    public LeaveApplication createLeave(LeaveDTO dto) throws IOException {

        // 🔒 1. Check pending count BEFORE saving files
        long pendingCount =
                repository.countByEmpIdAndStatusIn(dto.getEmpId(), PENDING_STATUSES);

        if (pendingCount >= MAX_PENDING) {
            throw new MaxPendingApplicationsException(
                    "Maximum pending Leave applications (" + MAX_PENDING + ") reached. " +
                    "Please wait for existing requests to be processed."
            );
        }

        // 📁 2. Prepare directory
        File empDir =
                fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // 📎 3. Save new files
        List<String> savedFiles =
                fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        String finalFileNames = String.join(";", savedFiles);

        LeaveApplication leave = new LeaveApplication();
        leave.updateFromDTO(dto, finalFileNames);
        leave.setStatus("PENDING");

        try {
            // 💾 4. DB save
            return repository.save(leave);
        } catch (RuntimeException ex) {
            // ❗ DB failed → rollback uploaded files
            fileStorageService.deleteFilesByNames(empDir, savedFiles);
            throw ex;
        }
    }

    /**
     * UPDATE existing leave application
     * ✅ Always allowed (even if MAX_PENDING reached)
     */
    @Override
    public LeaveApplication updateLeave(String applnNo, LeaveDTO dto) throws IOException {

        LeaveApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() ->
                        new RuntimeException("No application found with token: " + applnNo));

        File empDir =
                fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // Previously linked files
        Set<String> previouslyLinked =
                fileStorageService.parseRetainedFiles(existing.getFileName());

        // Files user wants to keep
        Set<String> retained =
                fileStorageService.parseRetainedFiles(dto.getRetainedFiles());

        // Save new files
        List<String> newFiles =
                fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        // Delete removed files (only for this application)
        fileStorageService.deleteRemovedFilesForApplication(
                previouslyLinked, retained, empDir
        );

        String finalFileNames =
                fileStorageService.mergeFileNames(retained, newFiles);

        existing.updateFromDTO(dto, finalFileNames);

        try {
            return repository.save(existing);
        } catch (RuntimeException ex) {
            // Rollback newly uploaded files
            fileStorageService.deleteFilesByNames(empDir, newFiles);
            throw ex;
        }
    }

    @Override
    public LeaveApplication updateStatus(String applnNo, String status) {
        LeaveApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() ->
                        new RuntimeException("No application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
