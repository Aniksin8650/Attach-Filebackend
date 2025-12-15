package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
import com.example.attachfile.exception.MaxPendingApplicationsException;
import com.example.attachfile.repository.LTCApplicationRepository;
import com.example.attachfile.service.FileStorageService;
import com.example.attachfile.service.LTCApplicationService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LTCApplicationServiceImpl implements LTCApplicationService {

    private final LTCApplicationRepository repository;
    private final FileStorageService fileStorageService;

    private static final int MAX_PENDING = 3;
    private static final Set<String> PENDING_STATUSES =
            Set.of("PENDING", "FORWARDED", "SUBMITTED");

    public LTCApplicationServiceImpl(LTCApplicationRepository repository,
                                     FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<LTCApplication> getAll() {
        return repository.findAll();
    }

    @Override
    public List<LTCApplication> getByEmpId(String empId) {
        return repository.findByEmpId(empId);
    }

    @Override
    public Optional<LTCApplication> getByApplnNo(String applnNo) {
        return repository.findByApplnNo(applnNo);
    }

    /**
     * CREATE new LTC application
     * ❌ Block if MAX_PENDING reached
     */
    @Override
    public LTCApplication submit(LTCDTO dto) throws IOException {

        // 🔒 1. Check pending limit BEFORE file operations
        long pendingCount =
                repository.countByEmpIdAndStatusIn(dto.getEmpId(), PENDING_STATUSES);

        if (pendingCount >= MAX_PENDING) {
            throw new MaxPendingApplicationsException(
                    "Maximum pending LTC applications (" + MAX_PENDING + ") reached. " +
                    "Please wait for existing requests to be processed."
            );
        }

        File empDir =
                fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        var newFiles =
                fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        String finalFileNames = String.join(";", newFiles);

        LTCApplication entity = new LTCApplication();
        entity.updateFromDTO(dto, finalFileNames);
        entity.setStatus("PENDING");

        try {
            return repository.save(entity);
        } catch (RuntimeException ex) {
            fileStorageService.deleteFilesByNames(empDir, newFiles);
            throw ex;
        }
    }

    /**
     * UPDATE existing LTC application
     * ✅ Always allowed
     */
    @Override
    public LTCApplication update(String applnNo, LTCDTO dto) throws IOException {

        LTCApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() ->
                        new RuntimeException("No LTC application found with token: " + applnNo));

        File empDir =
                fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        Set<String> previouslyLinked =
                fileStorageService.parseRetainedFiles(existing.getFileName());

        Set<String> retained =
                fileStorageService.parseRetainedFiles(dto.getRetainedFiles());

        var newFiles =
                fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        fileStorageService.deleteRemovedFilesForApplication(
                previouslyLinked, retained, empDir
        );

        String finalFileNames =
                fileStorageService.mergeFileNames(retained, newFiles);

        existing.updateFromDTO(dto, finalFileNames);

        try {
            return repository.save(existing);
        } catch (RuntimeException ex) {
            fileStorageService.deleteFilesByNames(empDir, newFiles);
            throw ex;
        }
    }

    @Override
    public List<LTCApplication> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    @Override
    public LTCApplication updateStatus(String applnNo, String status) {
        LTCApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() ->
                        new RuntimeException("No LTC application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
