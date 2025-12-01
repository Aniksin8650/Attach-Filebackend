package com.example.attachfile.service;

import com.example.attachfile.dto.TADTO;
import org.springframework.validation.BindingResult;

public interface TAValidationService {

    void validateForCreate(TADTO dto, BindingResult bindingResult);

    void validateForUpdate(String applnNo, TADTO dto, BindingResult bindingResult);
}
