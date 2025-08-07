package com.example.demo.repository;

import com.example.demo.model.DocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
}
