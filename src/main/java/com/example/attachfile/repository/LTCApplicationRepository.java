package com.example.attachfile.repository;

import com.example.attachfile.entity.LTCApplication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LTCApplicationRepository extends JpaRepository<LTCApplication, Long> {
    Optional<LTCApplication> findByApplnNo(String ApplnNo);
    
    List<LTCApplication> findByStatus(String status);

    List<LTCApplication> findByEmpId(String empId);
    
    List<LTCApplication> findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String empId,
            LocalDate endDate,
            LocalDate startDate
    );

    long countByEmpIdAndStatus(String empId, String status);
 // LTCApplicationRepository.java
//    long countByEmployeeIdAndStatusIn(String employeeId, Set<String> statuses);

}
