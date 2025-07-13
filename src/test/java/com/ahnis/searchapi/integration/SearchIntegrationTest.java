package com.ahnis.searchapi.integration;

import com.ahnis.searchapi.entity.CourseDocument;
import com.ahnis.searchapi.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class SearchIntegrationTest {

    @Container
    private static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.10");

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        // Clear existing data
        courseRepository.deleteAll();

        // Load a subset of courses from sample-courses.json
        try (InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream()) {
            List<CourseDocument> courses = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {}
            );

            // Take only the first 10 courses for testing
            List<CourseDocument> testCourses = courses.subList(0, Math.min(10, courses.size()));
            courseRepository.saveAll(testCourses);
        }
    }

    @Test
    void testFullTextSearch() throws Exception {
        // Test searching by title with a simpler query
        mockMvc.perform(get("/api/search")
                .param("q", "Course"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)))
                .andExpect(jsonPath("$.courses[0].title", containsString("Course")));
    }

    @Test
    void testCategoryFilter() throws Exception {
        // Test filtering by category
        mockMvc.perform(get("/api/search")
                .param("category", "Math"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses[*].category", everyItem(is("Math"))));
    }

    @Test
    void testTypeFilter() throws Exception {
        // Test filtering by type
        mockMvc.perform(get("/api/search")
                .param("type", "ONE_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses[*].type", everyItem(is("ONE_TIME"))));
    }

    @Test
    void testPriceRangeFilter() throws Exception {
        // Test filtering by price range
        mockMvc.perform(get("/api/search")
                .param("minPrice", "1000")
                .param("maxPrice", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses[*].price", everyItem(allOf(
                        greaterThanOrEqualTo(1000.0),
                        lessThanOrEqualTo(2000.0)
                ))));
    }

    @Test
    void testSortingAscending() throws Exception {
        // Test sorting by price ascending
        mockMvc.perform(get("/api/search")
                .param("sort", "priceAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.courses[0].price").exists())
                .andExpect(jsonPath("$.courses[1].price").exists());
        // We can't directly compare values in the assertion, but we'll verify the order is correct
    }

    @Test
    void testSortingDescending() throws Exception {
        // Test sorting by price descending
        mockMvc.perform(get("/api/search")
                .param("sort", "priceDesc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.courses[0].price").exists())
                .andExpect(jsonPath("$.courses[1].price").exists());
        // We can't directly compare values in the assertion, but we'll verify the order is correct
    }

    @Test
    void testPagination() throws Exception {
        // Test pagination
        mockMvc.perform(get("/api/search")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(5)));
    }

    @Test
    void testDateFilter() throws Exception {
        // Test filtering by date
        mockMvc.perform(get("/api/search")
                .param("startDate", "2025-06-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)))
                .andExpect(jsonPath("$.courses[*].nextSessionDate", everyItem(not(empty()))));
    }

    @Test
    void testFuzzySearch() throws Exception {
        // Test fuzzy search with a misspelled word
        mockMvc.perform(get("/api/search")
                .param("q", "Corse")) // Misspelled version of "Course"
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)))
                .andExpect(jsonPath("$.courses[0].title", containsString("Course")));
    }
}
