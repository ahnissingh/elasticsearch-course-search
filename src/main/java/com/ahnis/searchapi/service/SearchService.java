package com.ahnis.searchapi.service;

import com.ahnis.searchapi.dto.SearchRequest;
import com.ahnis.searchapi.entity.CourseDocument;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for searching courses in Elasticsearch
 */
public interface SearchService {

    /**
     * Search for courses based on the provided search criteria
     *
     * @param searchRequest The search request containing query, filters, sorting, and pagination parameters
     * @return A page of CourseDocument objects matching the search criteria
     */
    Page<CourseDocument> searchCourses(SearchRequest searchRequest);

    /**
     * Get autocomplete suggestions for course titles
     *
     * @param partialTitle The partial title to get suggestions for
     * @param size The maximum number of suggestions to return
     * @return A list of suggested course titles
     */
    List<String> getSuggestions(String partialTitle, int size);
}
