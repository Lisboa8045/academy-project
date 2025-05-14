package com.academy.services;

import com.academy.dto.service.ServiceRequestDTO;
import com.academy.dto.service.ServiceResponseDTO;
import com.academy.exceptions.ServiceNotFoundException;
import com.academy.models.Service;
import com.academy.models.Tag;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private ServiceRepository serviceRepository;
    private TagRepository tagRepository;

    public ServiceService(ServiceRepository serviceRepository, TagRepository tagRepository) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
    }

    // Create
    public ServiceResponseDTO create(ServiceRequestDTO dto) {
        Service entity = mapToEntity(dto, new Service());
        return mapToResponse(serviceRepository.save(entity));
    }

    // Update
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        Service updated = mapToEntity(dto, existing);
        return mapToResponse(serviceRepository.save(updated));
    }

    // Read all
    public List<ServiceResponseDTO> getAll() {
        return serviceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Read one
    public ServiceResponseDTO getById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        return mapToResponse(service);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        service.removeAllTags(); // Disassociate the tags from the service
        serviceRepository.delete(service);
    }

    // Mapping methods
    private Service mapToEntity(ServiceRequestDTO dto, Service service) {
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setDiscount(dto.getDiscount());
        service.setNegotiable(dto.isNegotiable());
        service.setDuration(dto.getDuration());
        service.setServiceType(dto.getServiceType());

        if (dto.getTagNames() != null) {
            List<Tag> tags = tagRepository.findAllByNameIn(dto.getTagNames());
            service.setTags(tags);
        }

        return service;
    }

    private ServiceResponseDTO mapToResponse(Service service) {
        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDiscount(service.getDiscount());
        dto.setNegotiable(service.isNegotiable());
        dto.setDuration(service.getDuration());
        dto.setServiceType(service.getServiceType());
        dto.setTags(service.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }
}