package com.example.demo.controller;

import com.example.demo.model.DocumentEntity;
import com.example.demo.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository repository;

    @GetMapping
    public List<DocumentEntity> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<DocumentEntity> createDocument(@RequestBody DocumentEntity doc) {
        if (doc.getTitle() == null || doc.getTitle().trim().isEmpty()) {
            doc.setTitle("Untitled");
        }
        doc.setCreatedAt(LocalDateTime.now());
        DocumentEntity savedDoc = repository.save(doc);
        return ResponseEntity.ok(savedDoc);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deleteDocumentById(@PathVariable String id){
        repository.deleteById(id);
    }

}
