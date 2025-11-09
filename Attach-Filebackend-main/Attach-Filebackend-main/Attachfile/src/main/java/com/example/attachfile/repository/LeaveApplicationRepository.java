package com.example.attachfile.repository;

import com.example.attachfile.entity.LeaveApplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication , Long> {
	Optional<LeaveApplication> findByToken(String token);
	@Query("SELECT l FROM LeaveApplication l WHERE l.empId = :empId AND " +
		       "((:startDate BETWEEN l.startDate AND l.endDate) OR " +
		       "(:endDate BETWEEN l.startDate AND l.endDate) OR " +
		       "(l.startDate BETWEEN :startDate AND :endDate) OR " +
		       "(l.endDate BETWEEN :startDate AND :endDate))")
		List<LeaveApplication> findOverlappingLeaves(@Param("empId") String empId,
		                                             @Param("startDate") String startDate,
		                                             @Param("endDate") String endDate);

}
