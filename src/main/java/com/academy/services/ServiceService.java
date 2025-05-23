package com.academy.services;

import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.exceptions.ServiceNotFoundException;
import com.academy.models.Member;
import com.academy.models.Service;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private ServiceRepository serviceRepository;
    private TagRepository tagRepository;
    private MemberRepository memberRepository; // Could also be MemberService
    private ServiceMapper serviceMapper;

    public ServiceService(ServiceRepository serviceRepository, TagRepository tagRepository, MemberRepository memberRepository,
                          ServiceMapper serviceMapper) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
        this.memberRepository = memberRepository;
        this.serviceMapper = serviceMapper;
    }

    // Create
    public ServiceResponseDTO create(ServiceRequestDTO dto, Long ownerId) {
        Service service = serviceMapper.toEntity(dto);
        Member owner = memberRepository.findById(ownerId).orElseThrow(); // TODO Create specific exception
        service.setOwner(owner);

        Service savedService = serviceRepository.save(service);
        return serviceMapper.toDto(savedService);
    }

    // Update
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        Service updated = serviceMapper.toEntity(dto);
        updated.setId(existing.getId());  // Retain existing ID
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

        service.removeAllTags(); // Disassociate the tags from the service
        serviceRepository.delete(service);
    }
}