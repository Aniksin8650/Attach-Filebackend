package com.example.attachfile.repository;

import com.example.attachfile.entity.TAApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TAApplicationRepository extends JpaRepository<TAApplication, Long> {

    Optional<TAApplication> findByApplnNo(String ApplnNo);
}
