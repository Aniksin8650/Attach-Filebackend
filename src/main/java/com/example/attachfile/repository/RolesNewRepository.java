package com.example.attachfile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attachfile.entity.RolesNew;

public interface RolesNewRepository extends JpaRepository<RolesNew, Long> {
    List<RolesNew> findByPersno(String persno);
}

