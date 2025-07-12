package com.ahnis.searchapi.service.impl;

import com.ahnis.searchapi.dto.SearchRequest;
import com.ahnis.searchapi.entity.CourseDocument;
import com.ahnis.searchapi.repository.CourseRepository;
import com.ahnis.searchapi.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * Implementation of {@link SearchService} that provides search capabilities for {@link CourseDocument}.
 *
 * <p>This service offers two main search modes:</p>
 * <ul>
 *     <li><b>Full-text Search:</b> Uses a multi-field search on course titles and descriptions when a text query is provided.</li>
 *     <li><b>Filtered Search:</b> Applies a combination of filters such as category, type, age range, price range, and session date.</li>
 * </ul>
 *
 * <p><b>Sorting:</b></p>
 * <ul>
 *     <li>Default: ascending by {@code nextSessionDate}</li>
 *     <li>priceAsc: ascending by {@code price}</li>
 *     <li>priceDesc: descending by {@code price}</li>
 * </ul>
 *
 * <p><b>Pagination:</b> Supports paginated results via {@code page} and {@code size} parameters from {@link SearchRequest}.</p>
 *
 * <p><b>Logic Flow:</b></p>
 * <ol>
 *     <li>Construct pageable object with appropriate sorting.</li>
 *     <li>If a text query is provided, run a full-text search on title and description.</li>
 *     <li>If additional filters exist or text search yields no results, fallback to filtered queries.</li>
 *     <li>Apply combinations of filters if available, else fallback to individual filter queries.</li>
 * </ol>
 *
 * <p><b>Filter Combinations Supported:</b></p>
 * <ul>
 *     <li>Category + Type + Date</li>
 *     <li>MinAge + MaxAge</li>
 *     <li>MinPrice + MaxPrice</li>
 * </ul>
 *
 * <p>If no criteria are specified, all courses are returned.</p>
 *
 * <p><b>Dependencies:</b></p>
 * <ul>
 *     <li>{@link CourseRepository} for Elasticsearch interactions</li>
 * </ul>
 *
 * <p><b>Note:</b> Uses modern switch expressions and expressive comments for an extra touch of developer personality ✨</p>
 *
 * @author Ahnis Singh Aneja
 * @since 2025
 *
 * <p><b>Fun Fact:</b> This service filters courses better than your dating app filters matches 💔📚</p>
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final CourseRepository courseRepository;

    @Override
    public Page<CourseDocument> searchCourses(SearchRequest searchRequest) {
        // Create pageable with sorting
        var pageable = createPageable(searchRequest);

        // If we have a text query, use it as the primary search method :)
        if (StringUtils.hasText(searchRequest.getQuery())) {
            return findCoursesWithTextSearch(searchRequest, pageable);
        }

        // Otherwise, we use 'filter' combo
        return findCoursesWithFilters(searchRequest, pageable);
    }

    private Pageable createPageable(SearchRequest searchRequest) {
        // Determine sort 'direction' and field (Just like how I am doing right now in life)
        Sort sort;
        if (searchRequest.getSort() != null) {
            // Using the  cool new switch expression like a true Gen Z dev 😎
            sort = switch (searchRequest.getSort()) {
                case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
                case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
                default -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
            };
        } else {
            // Default sort by nextSessionDate ascending
            sort = Sort.by(Sort.Direction.ASC, "nextSessionDate");
        }

        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }

    private Page<CourseDocument> findCoursesWithTextSearch(SearchRequest searchRequest, Pageable pageable) {
        String query = searchRequest.getQuery();
        log.debug("Searching courses with text query: {}", query);

        // Use the custom query method for text search
        Page<CourseDocument> results = courseRepository.findByTitleContainingOrDescriptionContaining(
                query, query, pageable);

        // Apply additional filters if needed — because even Elasticsearch deserves high standards 😌
        if (results.isEmpty() || hasAdditionalFilters(searchRequest)) {
            return findCoursesWithFilters(searchRequest, pageable);
        }

        return results;
    }

    private boolean hasAdditionalFilters(SearchRequest searchRequest) {
        return StringUtils.hasText(searchRequest.getCategory()) ||
                StringUtils.hasText(searchRequest.getType()) ||
                searchRequest.getMinAge() != null ||
                searchRequest.getMaxAge() != null ||
                searchRequest.getMinPrice() != null ||
                searchRequest.getMaxPrice() != null ||
                searchRequest.getFromDate() != null;
    }

    private Page<CourseDocument> findCoursesWithFilters(SearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching courses with filters");

        // Check for combined filters

        // Category and Type filter combination
        if (StringUtils.hasText(searchRequest.getCategory()) && StringUtils.hasText(searchRequest.getType())) {
            // If we also have a date filter — because even courses need a proper schedule, unlike my love life 😅
            if (searchRequest.getFromDate() != null) {
                return courseRepository.findByCategoryAndTypeAndNextSessionDateGreaterThanEqual(
                        searchRequest.getCategory(),
                        searchRequest.getType(),
                        searchRequest.getFromDate(),
                        pageable);
            }

            return courseRepository.findByCategoryAndType(
                    searchRequest.getCategory(),
                    searchRequest.getType(),
                    pageable);
        }

        // Age range filter combination
        if (searchRequest.getMinAge() != null && searchRequest.getMaxAge() != null) {
            return courseRepository.findByMinAgeGreaterThanEqualAndMaxAgeLessThanEqual(
                    searchRequest.getMinAge(),
                    searchRequest.getMaxAge(),
                    pageable);
        }

        // Price range filter combination
        if (searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
            return courseRepository.findByPriceGreaterThanEqualAndPriceLessThanEqual(
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    pageable);
        }

        // Apply individual filters if no combinations match

        // Category filter
        if (StringUtils.hasText(searchRequest.getCategory())) {
            return courseRepository.findByCategory(searchRequest.getCategory(), pageable);
        }

        // Type filter
        if (StringUtils.hasText(searchRequest.getType())) {
            return courseRepository.findByType(searchRequest.getType(), pageable);
        }

        // Min Age filter
        if (searchRequest.getMinAge() != null) {
            return courseRepository.findByMinAgeGreaterThanEqual(searchRequest.getMinAge(), pageable);
        }

        // Max Age filter
        if (searchRequest.getMaxAge() != null) {
            return courseRepository.findByMaxAgeLessThanEqual(searchRequest.getMaxAge(), pageable);
        }

        // Min Price filter
        if (searchRequest.getMinPrice() != null) {
            return courseRepository.findByPriceGreaterThanEqual(searchRequest.getMinPrice(), pageable);
        }

        // Max Price filter
        if (searchRequest.getMaxPrice() != null) {
            return courseRepository.findByPriceLessThanEqual(searchRequest.getMaxPrice(), pageable);
        }

        // Date filter
        if (searchRequest.getFromDate() != null) {
            return courseRepository.findByNextSessionDateGreaterThanEqual(searchRequest.getFromDate(), pageable);
        }

        // If no filters are applied, return all courses
        return courseRepository.findAll(pageable);
    }
}
