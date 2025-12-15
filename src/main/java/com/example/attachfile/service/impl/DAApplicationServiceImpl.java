package com.example.attachfile.service.impl;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;
import com.example.attachfile.exception.MaxPendingApplicationsException;
import com.example.attachfile.repository.DAApplicationRepository;
import com.example.attachfile.service.DAApplicationService;
import com.example.attachfile.service.FileStorageService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DAApplicationServiceImpl implements DAApplicationService {

    private final DAApplicationRepository repository;
    private final FileStorageService fileStorageService;

    private static final int MAX_PENDING = 3;
    private static final Set<String> PENDING_STATUSES =
            Set.of("PENDING", "FORWARDED", "SUBMITTED");

    public DAApplicationServiceImpl(DAApplicationRepository repository,
                                    FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<DAApplication> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<DAApplication> getByApplnNo(String applnNo) {
        return repository.findByApplnNo(applnNo);
    }

    /**
     * CREATE new DA application
     * ❌ Block if MAX_PENDING reached
     */
    @Override
    public DAApplication submit(DADTO dto) throws IOException {

        // 🔒 1. Check pending limit BEFORE file operations
        long pendingCount =
                repository.countByEmpIdAndStatusIn(dto.getEmpId(), PENDING_STATUSES);

        if (pendingCount >= MAX_PENDING) {
            throw new MaxPendingApplicationsException(
                    "Maximum pending DA applications (" + MAX_PENDING + ") reached. " +
                    "Please wait for existing requests to be processed."
            );
        }

        // 📁 2. Prepare directory
        File empDir =
                fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // 📎 3. Save new files
        var newFiles =
                fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        String finalFileNames = String.join(";", newFiles);

        DAApplication entity = new DAApplication();
        entity.updateFromDTO(dto, finalFileNames);
        entity.setStatus("PENDING");

        try {
            return repository.save(entity);
        } catch (RuntimeException ex) {
            // ❗ Rollback uploaded files
            fileStorageService.deleteFilesByNames(empDir, newFiles);
            throw ex;
        }
    }

    /**
     * UPDATE existing DA application
     * ✅ Always allowed
     */
    @Override
    public DAApplication update(String applnNo, DADTO dto) throws IOException {

        DAApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() ->
                        new RuntimeException("No DA application found with token: " + applnNo));

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
    public List<DAApplication> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    @Override
    public List<DAApplication> getByEmpId(String empId) {
        return repository.findByEmpId(empId);
    }

    @Override
    public DAApplication updateStatus(String applnNo, String status) {
        DAApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() ->
                        new RuntimeException("No DA application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
