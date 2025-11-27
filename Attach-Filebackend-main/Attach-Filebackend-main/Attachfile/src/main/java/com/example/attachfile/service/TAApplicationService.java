package com.example.attachfile.service;

import com.example.attachfile.dto.TADTO;
import com.example.attachfile.entity.TAApplication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TAApplicationService {

    List<TAApplication> getAll();

    Optional<TAApplication> getByApplnNo(String ApplnNo);

    TAApplication submit(TADTO dto) throws IOException;

    TAApplication update(String token, TADTO dto) throws IOException;
    
    List<TAApplication> getByStatus(String status);
    TAApplication updateStatus(String applnNo, String status);

}
