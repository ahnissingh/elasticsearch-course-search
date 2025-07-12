package com.ahnis.searchapi.bootstrap;

import com.ahnis.searchapi.entity.CourseDocument; // Add your CourseDocument entity import
import com.ahnis.searchapi.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.data.courses-file:sample-courses.json}")
    private String coursesFileName;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting data loading process...");

        // Check if courses already exist to avoid duplicates
        if (courseRepository.count() > 0) {
            log.info("Courses already exist in the database. Skipping data loading.");
            return;
        }

        try (InputStream inputStream = new ClassPathResource(coursesFileName).getInputStream()) {
            // Map JSON array to List<CourseDocument>
            List<CourseDocument> courses = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            log.info("Loaded {} courses from JSON file", courses.size());

            // Save these  courses to Elasticsearch
            List<CourseDocument> savedCourses = (List<CourseDocument>) courseRepository.saveAll(courses);

            log.info("Successfully saved {} courses to Elasticsearch", savedCourses.size());

        } catch (IOException e) {
            log.error("Error loading courses from JSON file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load course data", e);
        }
    }
}
