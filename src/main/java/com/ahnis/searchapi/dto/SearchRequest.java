package com.ahnis.searchapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for encapsulating course search parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//todo refactor to records :)
public class SearchRequest {
    // Full-text search query for title and description
    private String query;

    // Filters
    private String category;
    private String type;
    private Integer minAge;
    private Integer maxAge;
    private Double minPrice;
    private Double maxPrice;
    private Instant fromDate; // For nextSessionDate filter

    private String sort; // Possible values: null (default), "priceAsc", "priceDesc"

    // Pagination
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}
