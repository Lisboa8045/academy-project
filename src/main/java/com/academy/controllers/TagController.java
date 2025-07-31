package com.academy.controllers;

import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.services.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('WORKER')")
    @PostMapping
    public ResponseEntity<TagResponseDTO> create(@Valid @RequestBody TagRequestDTO dto) {
        return ResponseEntity.ok(tagService.create(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody TagRequestDTO dto) {
        TagResponseDTO response = tagService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDTO>> getAll() {
        List<TagResponseDTO> responses = tagService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> getById(@PathVariable Long id) {
        TagResponseDTO response = tagService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
