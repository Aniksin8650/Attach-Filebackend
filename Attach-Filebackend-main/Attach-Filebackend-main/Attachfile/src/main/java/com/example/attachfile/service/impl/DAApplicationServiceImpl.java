package com.example.attachfile.service.impl;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;
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

    @Override
    public DAApplication submit(DADTO dto) throws IOException {

        // Prepare directory
        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // Save new files
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        String finalFileNames = String.join(";", newFiles);

        DAApplication entity = new DAApplication();
        entity.updateFromDTO(dto, finalFileNames);

        // Default status for new DA application
        entity.setStatus("PENDING");

        return repository.save(entity);
    }

    @Override
    public DAApplication update(String applnNo, DADTO dto) throws IOException {

        DAApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() -> new RuntimeException("No DA application found with token: " + applnNo));

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

        // Optional: reset to pending on edit
        // existing.setStatus("PENDING");

        return repository.save(existing);
    }

    @Override
    public List<DAApplication> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    @Override
    public DAApplication updateStatus(String applnNo, String status) {
        DAApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() -> new RuntimeException("No DA application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
