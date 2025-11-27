package com.example.attachfile.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileStorageService {

    @Value("${uploads.base-dir}")
    private String baseUploadDir;

    private final SimpleDateFormat sdf = new SimpleDateFormat("HHmmssddMMyyyy");

    // ============================================================
    // Create necessary directories and return empDir path
    // ============================================================
    public File getEmpDirectory(String applicationType, String empId) {

        String typeFolder = applicationType.toLowerCase().trim();

        File baseDir = new File(baseUploadDir);
        if (!baseDir.exists()) baseDir.mkdirs();

        File typeDir = new File(baseDir, typeFolder);
        if (!typeDir.exists()) typeDir.mkdirs();

        File empDir = new File(typeDir, empId);
        if (!empDir.exists()) empDir.mkdirs();

        return empDir;
    }

    // ============================================================
    // Clean file name: remove spaces, symbols
    // ============================================================
    private String cleanFileName(String original) {
        return StringUtils.cleanPath(Objects.requireNonNull(original))
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9._-]", "");
    }

    // ============================================================
    // Save uploaded files → returns list of saved filenames
    // ============================================================
    public List<String> saveNewFiles(MultipartFile[] files, File empDir) throws IOException {
        List<String> savedNames = new ArrayList<>();

        if (files == null) return savedNames;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String original = cleanFileName(file.getOriginalFilename());
            String timestamp = sdf.format(new Date());
            String finalName = timestamp + "_" + original;

            File dest = new File(empDir, finalName);
            file.transferTo(dest);

            savedNames.add(finalName);
        }

        return savedNames;
    }

    // ============================================================
    // Delete files removed by user (not in retainedFiles list)
    // ============================================================
    public void deleteRemovedFiles(Set<String> retainedFiles, File empDir) {
        File[] allFiles = empDir.listFiles();
        if (allFiles == null) return;

        for (File file : allFiles) {
            if (!retainedFiles.contains(file.getName())) {
                file.delete();  // delete unused file
            }
        }
    }

    // ============================================================
    // Merge retained+new into a single fileName string
    // ============================================================
    public String mergeFileNames(Set<String> retained, List<String> newFiles) {
        retained.addAll(newFiles);
        return String.join(";", retained);
    }

    // ============================================================
    // Convert retainedFiles string → Set
    // ============================================================
    public Set<String> parseRetainedFiles(String retainedFiles) {
        if (retainedFiles == null || retainedFiles.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(retainedFiles.split(";")));
    }
}
