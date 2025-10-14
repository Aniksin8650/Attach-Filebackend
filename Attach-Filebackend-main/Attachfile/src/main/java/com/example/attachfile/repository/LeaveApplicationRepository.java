package com.example.attachfile.repository;

import com.example.attachfile.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication , Long> {

}
