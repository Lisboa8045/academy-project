package com.academy.services;

import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.TagNotFoundException;
import com.academy.models.Tag;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;
import com.academy.models.Service;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // Create
    public TagResponseDTO create(TagRequestDTO dto) {
        Tag tag = mapToEntity(dto, new Tag());
        return mapToResponse(tagRepository.save(tag));
    }

    // Update
    public TagResponseDTO update(Long id, TagRequestDTO dto) {
        Tag existing = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        Tag updated = mapToEntity(dto, existing);
        return mapToResponse(tagRepository.save(updated));
    }

    // Read all
    public List<TagResponseDTO> getAll() {
        return tagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Read 1
    public TagResponseDTO getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        return mapToResponse(tag);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        tag.getServices().forEach(service -> service.getTags().remove(tag)); // Break relationship
        tagRepository.delete(tag);
    }

    // Mapping methods
    private Tag mapToEntity(TagRequestDTO dto, Tag tag) {
        tag.setName(dto.getName());
        tag.setIsCustom(dto.getIsCustom());
        return tag;
    }

    private TagResponseDTO mapToResponse(Tag tag) {
        TagResponseDTO dto = new TagResponseDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setIsCustom(tag.getIsCustom());
        dto.setServiceIds(tag.getServices().stream()
                .map(Service::getId)
                .collect(Collectors.toList()));
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        return dto;
    }
}