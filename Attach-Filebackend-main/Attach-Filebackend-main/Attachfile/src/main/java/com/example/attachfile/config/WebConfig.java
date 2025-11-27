package com.example.attachfile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${uploads.base-dir}")
    private String baseUploadDir;

    /**
     * Static resource handler for file downloads
     * Maps:  /uploads/**  ->  D:/LeaveApplications/...
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve "D:/LeaveApplications" (from application.properties) to absolute path
        Path uploadPath = Paths.get(baseUploadDir).toAbsolutePath();
        String uploadLocation = uploadPath.toUri().toString(); // e.g. file:/D:/LeaveApplications/

        System.out.println("Serving /uploads/** from: " + uploadLocation);

        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }

    /**
     * CORS configuration for React frontend
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
