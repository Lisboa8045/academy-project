package com.academy.services;

import com.academy.dtos.service_type.ServiceTypeMapper;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.ServiceType;
import com.academy.repositories.ServiceTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository,
                              ServiceTypeMapper serviceTypeMapper) {
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceTypeMapper = serviceTypeMapper;
    }

    // Create
    @Transactional
    public ServiceTypeResponseDTO create(ServiceTypeRequestDTO dto) {
        ServiceType serviceType = serviceTypeMapper.toEntity(dto);
        ServiceType saved = serviceTypeRepository.save(serviceType);
        return serviceTypeMapper.toDto(saved);
    }

    // Update
    @Transactional
    public ServiceTypeResponseDTO update(Long id, ServiceTypeRequestDTO dto) {
        ServiceType existing = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceType.class, id));

        serviceTypeMapper.updateEntityFromDto(dto, existing);
        ServiceType updated = serviceTypeRepository.save(existing);
        return serviceTypeMapper.toDto(updated);
    }

    // Read all
    public List<ServiceTypeResponseDTO> getAll() {
        return serviceTypeRepository.findAll()
                .stream()
                .map(serviceTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    // Read one
    public ServiceTypeResponseDTO getById(Long id) {
        ServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceType.class, id));
        return serviceTypeMapper.toDto(serviceType);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        ServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceType.class, id));
        serviceTypeRepository.delete(serviceType);
    }
}
