package com.example.attachfile.service.impl;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;
import com.example.attachfile.repository.DAApplicationRepository;
import org.springframework.stereotype.Service;
import com.example.attachfile.service.*;

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
    public Optional<DAApplication> getByApplnNo(String ApplnNo) {
        return repository.findByApplnNo(ApplnNo);
    }

    @Override
    public DAApplication submit(DADTO dto) throws IOException {
        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        String finalFileNames = String.join(";", newFiles);

        DAApplication entity = new DAApplication();
        entity.updateFromDTO(dto, finalFileNames);
        return repository.save(entity);
    }

    @Override
    public DAApplication update(String ApplnNo, DADTO dto) throws IOException {
        DAApplication existing = repository.findByApplnNo(ApplnNo)
                .orElseThrow(() -> new RuntimeException("No DA application found with token: " + ApplnNo));

        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        Set<String> retained = fileStorageService.parseRetainedFiles(dto.getRetainedFiles());
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        fileStorageService.deleteRemovedFiles(retained, empDir);

        String finalFileNames = fileStorageService.mergeFileNames(retained, newFiles);

        existing.updateFromDTO(dto, finalFileNames);
        return repository.save(existing);
    }
}
