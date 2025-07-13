package com.ahnis.searchapi.controller;

import com.ahnis.searchapi.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Suggest Controller Tests")
class SuggestControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @Test
    @DisplayName("Should return suggestions for partial title")
    void shouldReturnSuggestionsForPartialTitle() {
        // Arrange
        List<String> expectedSuggestions = Arrays.asList(
                "Course 1",
                "Course 2",
                "Course 3"
        );
        when(searchService.getSuggestions(eq("Cou"), anyInt())).thenReturn(expectedSuggestions);

        // Act
        ResponseEntity<List<String>> response = searchController.getSuggestions("Cou", 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<String> actualSuggestions = response.getBody();
        assertNotNull(actualSuggestions);
        assertEquals(3, actualSuggestions.size());
        assertEquals("Course 1", actualSuggestions.get(0));
        assertEquals("Course 2", actualSuggestions.get(1));
        assertEquals("Course 3", actualSuggestions.get(2));
    }

    @Test
    @DisplayName("Should return empty list when no suggestions found")
    void shouldReturnEmptyListWhenNoSuggestionsFound() {
        // Arrange
        when(searchService.getSuggestions(eq("xyz"), anyInt())).thenReturn(List.of());

        // Act
        ResponseEntity<List<String>> response = searchController.getSuggestions("xyz", 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<String> actualSuggestions = response.getBody();
        assertNotNull(actualSuggestions);
        assertEquals(0, actualSuggestions.size());
    }
}
