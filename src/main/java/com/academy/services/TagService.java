package com.academy.services;

import com.academy.dtos.tag.TagMapper;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.TagNotFoundException;
import com.academy.models.Tag;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    // Create
    @Transactional
    public TagResponseDTO create(TagRequestDTO dto) {
        Tag tag = tagMapper.toEntity(dto);

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }

    // Update
    @Transactional
    public TagResponseDTO update(Long id, TagRequestDTO dto) {
        Tag existing = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        Tag updated = tagMapper.toEntity(dto);
        updated.setId(existing.getId());  // Retain existing ID
        updated = tagRepository.save(updated);

        return tagMapper.toDto(updated);
    }

    // Read all
    public List<TagResponseDTO> getAll() {
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
    }

    // Read one
    public TagResponseDTO getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        return tagMapper.toDto(tag);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        tag.getServices().forEach(service -> service.getTags().remove(tag)); // Disassociate the service references
        tagRepository.delete(tag);
    }
}