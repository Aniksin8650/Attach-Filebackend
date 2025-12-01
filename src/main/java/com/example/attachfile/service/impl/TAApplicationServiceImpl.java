package com.example.attachfile.service.impl;

import com.example.attachfile.dto.TADTO;
import com.example.attachfile.entity.TAApplication;
import com.example.attachfile.repository.TAApplicationRepository;
import com.example.attachfile.service.*;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TAApplicationServiceImpl implements TAApplicationService {

    private final TAApplicationRepository repository;
    private final FileStorageService fileStorageService;

    public TAApplicationServiceImpl(TAApplicationRepository repository,
                                    FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<TAApplication> getAll() {
        return repository.findAll();
    }
    @Override
    public List<TAApplication> getByEmpId(String empId) {
        return repository.findByEmpId(empId);
    }

    @Override
    public Optional<TAApplication> getByApplnNo(String ApplnNo) {
        return repository.findByApplnNo(ApplnNo);
    }

    @Override
    public TAApplication submit(TADTO dto) throws IOException {

        // Prepare directory
        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // Save new files
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        String finalFileNames = String.join(";", newFiles);

        TAApplication entity = new TAApplication();
        entity.updateFromDTO(dto, finalFileNames);

        return repository.save(entity);
    }

    @Override
    public TAApplication update(String ApplnNo, TADTO dto) throws IOException {

        TAApplication existing = repository.findByApplnNo(ApplnNo)
                .orElseThrow(() -> new RuntimeException("No TA application found with token: " + ApplnNo));

        // Prepare directory
        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // Parse retained files
        Set<String> retained = fileStorageService.parseRetainedFiles(dto.getRetainedFiles());

        // Save new files
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        // Delete removed ones
        fileStorageService.deleteRemovedFiles(retained, empDir);

        // Merge final filenames
        String finalFileNames = fileStorageService.mergeFileNames(retained, newFiles);

        existing.updateFromDTO(dto, finalFileNames);

        return repository.save(existing);
    }
    // 🆕 Get applications by status (PENDING / APPROVED / REJECTED / ON_HOLD...)
    @Override
    public List<TAApplication> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    // 🆕 Update status for a given applnNo (used by admin controller)
    @Override
    public TAApplication updateStatus(String applnNo, String status) {
        TAApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() -> new RuntimeException("No TA application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
