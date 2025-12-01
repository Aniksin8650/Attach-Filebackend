package com.example.attachfile.service;

import com.example.attachfile.dto.DADTO;
import org.springframework.validation.BindingResult;

public interface DAValidationService {

    void validateForCreate(DADTO dto, BindingResult bindingResult);

    void validateForUpdate(String applnNo, DADTO dto, BindingResult bindingResult);
}
