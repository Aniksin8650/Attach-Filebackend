package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
import com.example.attachfile.repository.LTCApplicationRepository;
import org.springframework.stereotype.Service;
import com.example.attachfile.service.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
    import java.util.Optional;
import java.util.Set;

@Service
public class LTCApplicationServiceImpl implements LTCApplicationService {

    private final LTCApplicationRepository repository;
    private final FileStorageService fileStorageService;

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
    public Optional<LTCApplication> getByApplnNo(String ApplnNo) {
        return repository.findByApplnNo(ApplnNo);
    }

    @Override
    public LTCApplication submit(LTCDTO dto) throws IOException {
        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        String finalFileNames = String.join(";", newFiles);

        LTCApplication entity = new LTCApplication();
        entity.updateFromDTO(dto, finalFileNames);
        return repository.save(entity);
    }

    @Override
    public LTCApplication update(String ApplnNo, LTCDTO dto) throws IOException {
        LTCApplication existing = repository.findByApplnNo(ApplnNo)
                .orElseThrow(() -> new RuntimeException("No LTC application found with token: " + ApplnNo));

        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        Set<String> retained = fileStorageService.parseRetainedFiles(dto.getRetainedFiles());
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        fileStorageService.deleteRemovedFiles(retained, empDir);

        String finalFileNames = fileStorageService.mergeFileNames(retained, newFiles);

        existing.updateFromDTO(dto, finalFileNames);
        return repository.save(existing);
    }
}
