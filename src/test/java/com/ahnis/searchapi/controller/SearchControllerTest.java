package com.ahnis.searchapi.controller;

import com.ahnis.searchapi.dto.SearchRequest;
import com.ahnis.searchapi.dto.SearchResponse;
import com.ahnis.searchapi.entity.CourseDocument;
import com.ahnis.searchapi.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@DisplayName("Search Controller Tests")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<CourseDocument> sampleCourses;
    private Page<CourseDocument> samplePage;

    @BeforeEach
    void setUp() {
        // Create sample course documents
        CourseDocument course1 = CourseDocument.builder()
                .id("1")
                .title("Java Programming Basics")
                .category("Programming")
                .price(99.99)
                .nextSessionDate(Instant.parse("2024-12-01T10:00:00Z"))
                .build();

        CourseDocument course2 = CourseDocument.builder()
                .id("2")
                .title("Advanced Spring Boot")
                .category("Programming")
                .price(149.99)
                .nextSessionDate(Instant.parse("2024-12-15T14:00:00Z"))
                .build();

        CourseDocument course3 = CourseDocument.builder()
                .id("3")
                .title("Data Science Fundamentals")
                .category("Data Science")
                .price(199.99)
                .nextSessionDate(Instant.parse("2024-12-20T09:00:00Z"))
                .build();

        sampleCourses = Arrays.asList(course1, course2, course3);
        samplePage = new PageImpl<>(sampleCourses, PageRequest.of(0, 10), 3);
    }

    @Test
    @DisplayName("Should search courses with basic query parameter")
    void shouldSearchCoursesWithBasicQuery() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("q", "Java")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses.length()").value(3))
                .andExpect(jsonPath("$.courses[0].id").value("1"))
                .andExpect(jsonPath("$.courses[0].title").value("Java Programming Basics"))
                .andExpect(jsonPath("$.courses[0].category").value("Programming"))
                .andExpect(jsonPath("$.courses[0].price").value(99.99));

        // Verify service was called with correct parameters
        verify(searchService).searchCourses(argThat(request ->
                "Java".equals(request.getQuery()) &&
                        request.getPage() == 0 &&
                        request.getSize() == 10
        ));
    }

    @Test
    @DisplayName("Should search courses with all filter parameters")
    void shouldSearchCoursesWithAllFilters() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("q", "programming")
                        .param("category", "Programming")
                        .param("type", "online")
                        .param("minAge", "18")
                        .param("maxAge", "65")
                        .param("minPrice", "50.0")
                        .param("maxPrice", "200.0")
                        .param("startDate", "2024-12-01T00:00:00Z")
                        .param("sort", "priceAsc")
                        .param("page", "1")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.courses").isArray());

        // Verify service was called with all parameters
        verify(searchService).searchCourses(argThat(request ->
                "programming".equals(request.getQuery()) &&
                        "Programming".equals(request.getCategory()) &&
                        "online".equals(request.getType()) &&
                        request.getMinAge().equals(18) &&
                        request.getMaxAge().equals(65) &&
                        request.getMinPrice().equals(50.0) &&
                        request.getMaxPrice().equals(200.0) &&
                        request.getFromDate().equals(Instant.parse("2024-12-01T00:00:00Z")) &&
                        "priceAsc".equals(request.getSort()) &&
                        request.getPage() == 1 &&
                        request.getSize() == 20
        ));
    }

    @Test
    @DisplayName("Should search courses with default pagination when no page parameters provided")
    void shouldUseDefaultPagination() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify default pagination values
        verify(searchService).searchCourses(argThat(request ->
                request.getPage() == 0 &&
                        request.getSize() == 10
        ));
    }

    @Test
    @DisplayName("Should search courses with custom pagination")
    void shouldSearchCoursesWithCustomPagination() throws Exception {
        // Given
        Page<CourseDocument> customPage = new PageImpl<>(
                sampleCourses.subList(0, 2),
                PageRequest.of(2, 5),
                15
        );
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(customPage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("page", "2")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(15))
                .andExpect(jsonPath("$.courses.length()").value(2));

        verify(searchService).searchCourses(argThat(request ->
                request.getPage() == 2 &&
                        request.getSize() == 5
        ));
    }

    @Test
    @DisplayName("Should return empty results when no courses found")
    void shouldReturnEmptyResultsWhenNoCoursesFound() throws Exception {
        // Given
        Page<CourseDocument> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("q", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses.length()").value(0));
    }

    @Test
    @DisplayName("Should handle price range filters correctly")
    void shouldHandlePriceRangeFilters() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("minPrice", "100.0")
                        .param("maxPrice", "500.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(searchService).searchCourses(argThat(request ->
                request.getMinPrice().equals(100.0) &&
                        request.getMaxPrice().equals(500.0)
        ));
    }

    @Test
    @DisplayName("Should handle age range filters correctly")
    void shouldHandleAgeRangeFilters() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("minAge", "21")
                        .param("maxAge", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(searchService).searchCourses(argThat(request ->
                request.getMinAge().equals(21) &&
                        request.getMaxAge().equals(50)
        ));
    }

    @Test
    @DisplayName("Should handle different sort parameters")
    void shouldHandleDifferentSortParameters() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // Test priceAsc
        mockMvc.perform(get("/api/search")
                        .param("sort", "priceAsc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(searchService).searchCourses(argThat(request ->
                "priceAsc".equals(request.getSort())
        ));

        // Test priceDesc
        mockMvc.perform(get("/api/search")
                        .param("sort", "priceDesc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(searchService).searchCourses(argThat(request ->
                "priceDesc".equals(request.getSort())
        ));
    }

    @Test
    @DisplayName("Should handle date filter correctly")
    void shouldHandleDateFilter() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);
        String testDate = "2024-12-01T10:00:00Z";

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("startDate", testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(searchService).searchCourses(argThat(request ->
                request.getFromDate().equals(Instant.parse(testDate))
        ));
    }

    @Test
    @DisplayName("Should validate response structure correctly")
    void shouldValidateResponseStructure() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.courses").exists())
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses[0].id").exists())
                .andExpect(jsonPath("$.courses[0].title").exists())
                .andExpect(jsonPath("$.courses[0].category").exists())
                .andExpect(jsonPath("$.courses[0].price").exists())
                .andExpect(jsonPath("$.courses[0].nextSessionDate").exists());
    }
//    todo write a global exception handler to fix this
//    @Test
//    @DisplayName("Should handle service exceptions gracefully")
//    void shouldHandleServiceExceptions() throws Exception {
//        // Given
//        when(searchService.searchCourses(any(SearchRequest.class)))
//                .thenThrow(new RuntimeException("Service unavailable"));
//
//        // When & Then
//        mockMvc.perform(get("/api/search")
//                        .param("q", "test")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().is5xxServerError());
//    }

    @Test
    @DisplayName("Should handle invalid date format")
    void shouldHandleInvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("startDate", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle invalid number parameters")
    void shouldHandleInvalidNumberParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("minPrice", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/search")
                        .param("page", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should verify service is called exactly once per request")
    void shouldVerifyServiceCallCount() throws Exception {
        // Given
        when(searchService.searchCourses(any(SearchRequest.class))).thenReturn(samplePage);

        // When
        mockMvc.perform(get("/api/search")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Then
        verify(searchService, times(1)).searchCourses(any(SearchRequest.class));
        verifyNoMoreInteractions(searchService);
    }
}
