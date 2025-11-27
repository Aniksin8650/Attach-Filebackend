package com.example.attachfile.repository;

import com.example.attachfile.entity.DAApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DAApplicationRepository extends JpaRepository<DAApplication, Long> {
    Optional<DAApplication> findByApplnNo(String ApplnNo);
}
