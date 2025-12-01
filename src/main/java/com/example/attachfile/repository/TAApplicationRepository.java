package com.example.attachfile.repository;

import com.example.attachfile.entity.TAApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TAApplicationRepository extends JpaRepository<TAApplication, Long> {

    Optional<TAApplication> findByApplnNo(String ApplnNo);
    
    List<TAApplication> findByStatus(String status);
    
    List<TAApplication> findByEmpId(String empId);
    
    List<TAApplication> findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String empId,
            LocalDate endDate,
            LocalDate startDate
    );



}
