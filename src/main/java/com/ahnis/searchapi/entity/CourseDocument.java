package com.ahnis.searchapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.time.Instant;

@Document(indexName = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDocument {
    @Id
    private String id;
    private String title;
    private String description;
    private String category;
    private String type;
    private String gradeRange;
    private Integer minAge;
    private Integer maxAge;
    private Double price;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Field(type = FieldType.Date, pattern = "uuuu-MM-dd'T'HH:mm:ss'Z'")
    private Instant nextSessionDate;

    @CompletionField(maxInputLength = 100)
    private Completion suggest;
}
