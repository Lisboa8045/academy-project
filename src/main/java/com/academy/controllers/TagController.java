package com.academy.controllers;

import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.services.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<TagResponseDTO> create(@Valid @RequestBody TagRequestDTO dto) {
        return ResponseEntity.ok(tagService.create(dto));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
