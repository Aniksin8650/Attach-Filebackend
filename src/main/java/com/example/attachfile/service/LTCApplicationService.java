package com.example.attachfile.service;

import com.example.attachfile.dto.LTCDTO;
import com.example.attachfile.entity.LTCApplication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface LTCApplicationService {

    List<LTCApplication> getAll();

    Optional<LTCApplication> getByApplnNo(String ApplnNo);

    LTCApplication submit(LTCDTO dto) throws IOException;

    LTCApplication update(String token, LTCDTO dto) throws IOException;
}
