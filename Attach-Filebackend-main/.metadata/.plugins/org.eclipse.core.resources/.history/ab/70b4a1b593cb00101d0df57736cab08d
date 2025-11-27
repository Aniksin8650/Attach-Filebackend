package com.example.attachfile.service;

import com.example.attachfile.dto.DADTO;
import com.example.attachfile.entity.DAApplication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DAApplicationService {

    List<DAApplication> getAll();

    Optional<DAApplication> getByApplnNo(String ApplnNo);

    DAApplication submit(DADTO dto) throws IOException;

    DAApplication update(String token, DADTO dto) throws IOException;
}
