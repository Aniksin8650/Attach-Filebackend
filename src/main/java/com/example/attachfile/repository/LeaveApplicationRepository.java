package com.example.attachfile.repository;

import com.example.attachfile.entity.LeaveApplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    Optional<LeaveApplication> findByApplnNo(String applnNo); // or findByToken if you haven't renamed yet

    @Query("""
           SELECT l 
           FROM LeaveApplication l
           WHERE l.empId = :empId
             AND l.startDate <= :endDate
             AND l.endDate   >= :startDate
           """)
    List<LeaveApplication> findOverlappingLeaves(
            @Param("empId") String empId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 🆕 For administrator Requests page: pending/approved/etc.
    List<LeaveApplication> findByStatus(String status);
    
    List<LeaveApplication> findByEmpId(String empId);
    
    List<LeaveApplication> findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String empId,
            LocalDate endDate,
            LocalDate startDate
    );

}
