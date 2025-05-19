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

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final TagService tagService;
    private final ServiceTypeRepository serviceTypeRepository; // TODO should probably change this to service once it's done

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
        service.setTags(tags);
        service.setServiceType(serviceType);

        // Add the service to each tag's services list
        for (Tag tag : tags) {
            tag.getServices().add(service);
        }

        Service savedService = serviceRepository.save(service);
        return serviceMapper.toDto(savedService);
    }

    // Update
    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        List<Tag> tags = tagService.findOrCreateTagsByNames(dto.getTagNames());

        ServiceType serviceType = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new ServiceTypeNotFoundException(dto.getServiceTypeId()));

        Service updated = serviceMapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTags(tags); // TODO fix the tag association here as well, similar to create
        updated.setServiceType(serviceType);

        updated = serviceRepository.save(updated);
        return serviceMapper.toDto(updated);
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

        service.removeAllTags(); // TODO test this functionality, might not be working properly
        serviceRepository.delete(service);
    }

    @Transactional
    public void dissociateTagFromAllServices(Tag tag) {
        List<Service> services = serviceRepository.findAllByTagsContaining(tag);
        for (Service service : services) {
            service.getTags().remove(tag);
        } // TODO again, double check this
        serviceRepository.saveAll(services);
    }
}