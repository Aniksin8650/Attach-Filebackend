package com.example.attachfile;  // same package as your main application

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.attachfile.repository.EmployeeRepository;
@Component
public class EmployeeTestRunner implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        // Test fetching EMP001
        employeeRepository.findByEmpId("EMP003").ifPresentOrElse(
            e -> System.out.println("Found: " + e.getName()),
            () -> System.out.println("Not found")
        );
    }
}
