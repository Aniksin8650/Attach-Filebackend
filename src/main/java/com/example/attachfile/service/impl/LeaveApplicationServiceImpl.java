package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LeaveDTO;
import com.example.attachfile.entity.LeaveApplication;
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

    @Override
    public LeaveApplication createLeave(LeaveDTO dto) throws IOException {

        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        List<String> savedFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        String finalFileNames = String.join(";", savedFiles);

        LeaveApplication leave = new LeaveApplication();
        leave.updateFromDTO(dto, finalFileNames);
        leave.setStatus("PENDING");

        try {
            // DB save
            return repository.save(leave);
        } catch (RuntimeException ex) {
            // ❗ DB failed → clean up the just-saved files
            fileStorageService.deleteFilesByNames(empDir, savedFiles);
            throw ex; // rethrow so controller can return error
        }
    }
    
    @Override
    public LeaveApplication updateLeave(String applnNo, LeaveDTO dto) throws IOException {

        LeaveApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() -> new RuntimeException("No application found with token: " + applnNo));

        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        Set<String> previouslyLinked =
                fileStorageService.parseRetainedFiles(existing.getFileName());

        Set<String> retained =
                fileStorageService.parseRetainedFiles(dto.getRetainedFiles());

        List<String> newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        // Only delete un-retained old files after we are confident about DB?
        fileStorageService.deleteRemovedFilesForApplication(previouslyLinked, retained, empDir);

        String finalFileNames = fileStorageService.mergeFileNames(retained, newFiles);
        existing.updateFromDTO(dto, finalFileNames);

        try {
            return repository.save(existing);
        } catch (RuntimeException ex) {
            // rollback newly uploaded files
            fileStorageService.deleteFilesByNames(empDir, newFiles);
            // (optional) you could also try to restore deleted old files, but that’s more complex)
            throw ex;
        }
    }

    @Override
    public LeaveApplication updateStatus(String applnNo, String status) {
        LeaveApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() -> new RuntimeException("No application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
