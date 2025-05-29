package com.academy.services;

import com.academy.dtos.tag.TagMapper;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class TagService {

    private final TagRepository tagRepository;
    private final ServiceRepository serviceRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, ServiceRepository serviceRepository, TagMapper tagMapper) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    // Create
    @Transactional
    public TagResponseDTO create(TagRequestDTO dto) {
        Tag tag = tagMapper.toEntity(dto);

        List<Service> services = serviceRepository.findAllById(dto.serviceIds());
        linkTagsToService(tag, services);

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }

    @Transactional
    public TagResponseDTO update(Long id, TagRequestDTO dto) {
        Tag existing = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, id));

        // Remove existing service associations
        for (Service service : new ArrayList<>(existing.getServices())) {
            service.getTags().remove(existing);
        }
        existing.getServices().clear();

        // Handle associations with services
        List<Service> newServices = serviceRepository.findAllById(dto.serviceIds());
        linkTagsToService(existing, newServices);

        tagMapper.updateEntityFromDto(dto, existing);

        return tagMapper.toDto(tagRepository.save(existing));
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
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, id));

        return tagMapper.toDto(tag);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, id));

        removeTagFromAllServices(tag); // Break relationship with services
        tagRepository.delete(tag);
    }

    // Get/Create a list of tags based on the names given on request
    @Transactional
    public List<Tag> findOrCreateTagsByNames(List<String> tagNames) {
        List<Tag> existingTags = tagRepository.findAllByNameIn(tagNames);
        List<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .toList();

        List<String> newTagNames = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .toList();

        // Create new tags for names that don't exist
        List<Tag> newTags = newTagNames.stream()
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    tag.setIsCustom(true);
                    return tag;
                })
                .toList();

        if (!newTags.isEmpty()) {
            try {
                tagRepository.saveAll(newTags); // Try inserting all new tags
            } catch (DataIntegrityViolationException e) {
                // Race condition likely occurred: // TODO log
            }
        }

        // Combine existing and new tags and return them
        List<Tag> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);
        return allTags;
    }

    @Transactional
    public void removeTagFromAllServices(Tag tag) {
        List<Service> services = new ArrayList<>(tag.getServices());
        tag.removeAllServices();
        serviceRepository.saveAll(services);
    }

    private void linkTagsToService(Tag tag, List<Service> services) {
        tag.setServices(services);
        for (Service service : services) {
            service.getTags().add(tag);
        }
    }
}