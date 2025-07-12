package com.ahnis.searchapi.service;

import com.ahnis.searchapi.dto.SearchRequest;
import com.ahnis.searchapi.entity.CourseDocument;
import org.springframework.data.domain.Page;

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
}
