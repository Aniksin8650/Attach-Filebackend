package com.example.attachfile.repository;

import com.example.attachfile.entity.LTCApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LTCApplicationRepository extends JpaRepository<LTCApplication, Long> {
    Optional<LTCApplication> findByApplnNo(String ApplnNo);
    
    List<LTCApplication> findByStatus(String status);

}
