package com.example.attachfile.service;

import com.example.attachfile.dto.LTCDTO;
import org.springframework.validation.BindingResult;

public interface LTCValidationService {

    void validateForCreate(LTCDTO dto, BindingResult bindingResult);

    void validateForUpdate(String applnNo, LTCDTO dto, BindingResult bindingResult);
}
