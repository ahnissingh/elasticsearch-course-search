package com.ahnis.searchapi.controller;

import com.ahnis.searchapi.dto.SearchRequest;
import com.ahnis.searchapi.dto.SearchResponse;
import com.ahnis.searchapi.entity.CourseDocument;
import com.ahnis.searchapi.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Searching courses with q: {}, category: {}, type: {}, minAge: {}, maxAge: {}, " +
                        "minPrice: {}, maxPrice: {}, startDate: {}, sort: {}, page: {}, size: {}",
                q, category, type, minAge, maxAge, minPrice, maxPrice, startDate, sort, page, size);

        // Create search request from parameters
        SearchRequest searchRequest = SearchRequest.builder()
                .query(q)
                .category(category)
                .type(type)
                .minAge(minAge)
                .maxAge(maxAge)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .fromDate(startDate)
                .sort(sort)
                .page(page)
                .size(size)
                .build();

        // Execute search
        Page<CourseDocument> results = searchService.searchCourses(searchRequest);

        log.info("Found {} courses", results.getTotalElements());

        // Convert to SearchResponse
        SearchResponse response = SearchResponse.builder()
                .total(results.getTotalElements())
                .courses(results.getContent().stream()
                        .map(SearchResponse.CourseInfo::fromCourseDocument)
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }
}
