package com.academy.services;

import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.exceptions.ServiceNotFoundException;
import com.academy.exceptions.ServiceTypeNotFoundException;
import com.academy.models.Service;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final TagService tagService;
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceService(ServiceRepository serviceRepository, ServiceMapper serviceMapper, TagService tagService, ServiceTypeRepository serviceTypeRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
        this.tagService = tagService;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    // Create
    @Transactional
    public ServiceResponseDTO create(ServiceRequestDTO dto) {
        List<Tag> tags = tagService.findOrCreateTagsByNames(dto.getTagNames());
        ServiceType serviceType = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new ServiceTypeNotFoundException(dto.getServiceTypeId()));

        Service service = serviceMapper.toEntity(dto);
        service.setServiceType(serviceType);
        linkServiceToTags(service, tags); // Set up the bidirectional link

        Service savedService = serviceRepository.save(service);
        return serviceMapper.toDto(savedService);
    }

    // Update
    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        // Remove existing tag associations
        for (Tag tag : new ArrayList<>(existing.getTags())) {
            tag.getServices().remove(existing);
        }
        existing.getTags().clear();

        // Prepare new tags and associations
        List<Tag> newTags = tagService.findOrCreateTagsByNames(dto.getTagNames());
        linkServiceToTags(existing, newTags);

        ServiceType serviceType = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new ServiceTypeNotFoundException(dto.getServiceTypeId()));

        existing.setServiceType(serviceType);
        serviceMapper.updateEntityFromDto(dto, existing);

        return serviceMapper.toDto(serviceRepository.save(existing));
    }

    // Read all
    public List<ServiceResponseDTO> getAll() {
        return serviceRepository.findAll()
                .stream()
                .map(serviceMapper::toDto)
                .collect(Collectors.toList());
    }

    // Read one
    public ServiceResponseDTO getById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        return serviceMapper.toDto(service);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        service.removeAllTags();
        serviceRepository.delete(service);
    }

    private void linkServiceToTags(Service service, List<Tag> tags) {
        service.setTags(tags);
        for (Tag tag : tags) {
            tag.getServices().add(service);
        }
    }
}