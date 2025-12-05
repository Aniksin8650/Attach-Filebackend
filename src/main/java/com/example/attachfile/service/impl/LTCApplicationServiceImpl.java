package com.example.attachfile.service.impl;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;
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

    @Override
    public LTCApplication submit(LTCDTO dto) throws IOException {

        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);
        String finalFileNames = String.join(";", newFiles);

        LTCApplication entity = new LTCApplication();
        entity.updateFromDTO(dto, finalFileNames);

        entity.setStatus("PENDING");

        try {
            return repository.save(entity);
        } catch (RuntimeException ex) {
            // DB/transaction failed → rollback newly saved files
            fileStorageService.deleteFilesByNames(empDir, newFiles);
            throw ex;
        }
    }


    @Override
    public LTCApplication update(String applnNo, LTCDTO dto) throws IOException {

        LTCApplication existing = repository.findByApplnNo(applnNo)
                .orElseThrow(() -> new RuntimeException("No LTC application found with token: " + applnNo));

        File empDir = fileStorageService.getEmpDirectory(dto.getApplicationType(), dto.getEmpId());

        // 🔹 Files previously linked to THIS LTC application (from DB)
        Set<String> previouslyLinked =
                fileStorageService.parseRetainedFiles(existing.getFileName());

        // 🔹 Files user decided to KEEP this time (coming from frontend)
        Set<String> retained =
                fileStorageService.parseRetainedFiles(dto.getRetainedFiles());

        // 🔹 Newly uploaded files
        var newFiles = fileStorageService.saveNewFiles(dto.getFiles(), empDir);

        // 🔹 Delete only those old files of THIS application that are not retained
        fileStorageService.deleteRemovedFilesForApplication(previouslyLinked, retained, empDir);

        // 🔹 Final filenames = retained old + new
        String finalFileNames = fileStorageService.mergeFileNames(retained, newFiles);

        existing.updateFromDTO(dto, finalFileNames);
        // Optional: existing.setStatus("PENDING");

        try {
            return repository.save(existing);
        } catch (RuntimeException ex) {
            // DB/transaction failed → remove newly uploaded files
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
                .orElseThrow(() -> new RuntimeException("No LTC application found with token: " + applnNo));

        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }
}
