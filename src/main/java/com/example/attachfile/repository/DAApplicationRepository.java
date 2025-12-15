package com.example.attachfile.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attachfile.entity.DAApplication;

public interface DAApplicationRepository extends JpaRepository<DAApplication, Long> {

    Optional<DAApplication> findByApplnNo(String applnNo);

    List<DAApplication> findByStatus(String status);

    List<DAApplication> findByEmpId(String empId);

    // Overlapping applications for CREATE:
    // existing.startDate <= newEndDate AND existing.endDate >= newStartDate
    List<DAApplication> findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String empId,
            LocalDate endDate,
            LocalDate startDate
    );

    long countByEmpIdAndStatus(String empId, String status);

 // DAApplicationRepository.java
    long countByEmpIdAndStatusIn(String empId, Set<String> statuses);

}
