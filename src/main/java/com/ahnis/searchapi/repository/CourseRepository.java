package com.ahnis.searchapi.repository;

import com.ahnis.searchapi.entity.CourseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;

public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {

    /**
     * Find courses by matching title or description with the given query
     */
    Page<CourseDocument> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);

    /**
     * Find courses by category
     */
    Page<CourseDocument> findByCategory(String category, Pageable pageable);

    /**
     * Find courses by type
     */
    Page<CourseDocument> findByType(String type, Pageable pageable);

    /**
     * Find courses by minAge greater than or equal to the given value
     */
    Page<CourseDocument> findByMinAgeGreaterThanEqual(Integer minAge, Pageable pageable);

    /**
     * Find courses by maxAge less than or equal to the given value
     */
    Page<CourseDocument> findByMaxAgeLessThanEqual(Integer maxAge, Pageable pageable);

    /**
     * Find courses by price greater than or equal to the given value
     */
    Page<CourseDocument> findByPriceGreaterThanEqual(Double minPrice, Pageable pageable);

    /**
     * Find courses by price less than or equal to the given value
     */
    Page<CourseDocument> findByPriceLessThanEqual(Double maxPrice, Pageable pageable);

    /**
     * Find courses by nextSessionDate greater than or equal to the given value
     */
    Page<CourseDocument> findByNextSessionDateGreaterThanEqual(Instant fromDate, Pageable pageable);

    /**
     * Find courses with combined filters
     */
    Page<CourseDocument> findByCategoryAndType(String category, String type, Pageable pageable);

    /**
     * Find courses with combined age range filters
     */
    Page<CourseDocument> findByMinAgeGreaterThanEqualAndMaxAgeLessThanEqual(Integer minAge, Integer maxAge, Pageable pageable);

    /**
     * Find courses with combined price range filters
     */
    Page<CourseDocument> findByPriceGreaterThanEqualAndPriceLessThanEqual(Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Find courses with category, type and date filters
     */
    Page<CourseDocument> findByCategoryAndTypeAndNextSessionDateGreaterThanEqual(
            String category, String type, Instant fromDate, Pageable pageable);
}
