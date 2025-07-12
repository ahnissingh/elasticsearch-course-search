package com.ahnis.searchapi.dto;

import com.ahnis.searchapi.entity.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for encapsulating course search results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//todo refactor to records :) , it looks cleaner and smooth!!!
public class SearchResponse {
    // Total number of hits
    private long total;

    // List of matching course documents
    private List<CourseInfo> courses;

    /**
     * Inner class representing the course information to be returned in the response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseInfo {
        private String id;
        private String title;
        private String category;
        private Double price;
        private Instant nextSessionDate;

        /**
         * Factory method to create a CourseInfo from a CourseDocument
         */
        public static CourseInfo fromCourseDocument(CourseDocument courseDocument) {
            return CourseInfo.builder()
                    .id(courseDocument.getId())
                    .title(courseDocument.getTitle())
                    .category(courseDocument.getCategory())
                    .price(courseDocument.getPrice())
                    .nextSessionDate(courseDocument.getNextSessionDate())
                    .build();
        }
    }
}
