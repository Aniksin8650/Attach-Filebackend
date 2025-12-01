package com.example.attachfile.service;

import com.example.attachfile.dto.LeaveDTO;
import org.springframework.validation.BindingResult;

public interface LeaveValidationService {

    void validateForCreate(LeaveDTO dto, BindingResult bindingResult);

    void validateForUpdate(String applnNo, LeaveDTO dto, BindingResult bindingResult);
}
