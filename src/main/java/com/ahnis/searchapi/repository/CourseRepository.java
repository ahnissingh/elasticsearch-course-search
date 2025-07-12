package com.ahnis.searchapi.repository;

import com.ahnis.searchapi.entity.CourseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {
}

