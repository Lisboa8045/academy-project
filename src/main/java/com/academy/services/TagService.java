package com.academy.services;

import com.academy.dtos.tag.TagMapper;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final ServiceService serviceService;

    public TagService(TagRepository tagRepository, TagMapper tagMapper, @Lazy ServiceService serviceService) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
        this.serviceService = serviceService;
    }

    // Create
    @Transactional
    public TagResponseDTO create(TagRequestDTO dto) {
        Tag tag = tagMapper.toEntity(dto);

        linkTagsToService(tag, dto.serviceIds());

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }

    @Transactional
    public TagResponseDTO update(Long id, TagRequestDTO dto) {
        Tag existing = getTagEntityById(id);

        linkTagsToService(existing, dto.serviceIds());

        tagMapper.updateEntityFromDto(dto, existing);
        return tagMapper.toDto(tagRepository.save(existing));
    }

    // Read all
    public List<TagResponseDTO> getAll() {
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toDto)
                .toList();
    }

    // Read one
    public TagResponseDTO getById(Long id) {
        Tag tag = getTagEntityById(id);

        return tagMapper.toDto(tag);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        Tag tag = getTagEntityById(id);

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
        List<Tag> newTags = createTagsFromNames(newTagNames);
        tagRepository.saveAll(newTags);

        // Combine existing and new tags and return them
        List<Tag> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);
        return allTags;
    }

    public List<Tag> createTagsFromNames(List<String> tagNames) {
        return tagNames.stream()
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    tag.setCustom(true);
                    return tag;
                })
                .toList();
    }

    public Tag getTagEntityById(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Tag.class, id));
    }

    public Tag getTagEntityByName(String name) {
        return tagRepository.findByName(name).orElseThrow(() -> new EntityNotFoundException(Tag.class, " with name " + name + " not found"));
    }

    private void removeTagFromAllServices(Tag tag) {
        for (Service service : new ArrayList<>(tag.getServices())) {
            service.getTags().remove(tag);
        }
        tag.getServices().clear();
    }

    private void linkTagsToService(Tag tag, List<Long> serviceIds) {
        removeTagFromAllServices(tag);

        List<Service> services = serviceService.getServiceEntitiesByIds(serviceIds);
        tag.setServices(services);
        for (Service service : services) {
            service.getTags().add(tag);
        }
    }
}