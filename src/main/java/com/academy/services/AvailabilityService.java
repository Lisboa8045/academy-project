package com.academy.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.academy.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.dtos.availability.AvailabilityMapper;
import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Availability;
import com.academy.repositories.AvailabilityRepository;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.repositories.ServiceRepository;

import jakarta.transaction.Transactional;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceRepository serviceRepository;
    private final MemberRepository memberRepository;
    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            ServiceProviderRepository serviceProviderRepository,
            MemberRepository memberRepository,
            ServiceRepository serviceRepository,
            AvailabilityMapper availabilityMapper) {

        this.availabilityRepository = availabilityRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.memberRepository = memberRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityMapper = availabilityMapper;
    }

    // Get all availabilities for a specific member by their ID
    public List<AvailabilityResponseDTO> getAvailabilitiesByMemberId(Long memberId) {
        if (memberId == null) {
            throw new InvalidArgumentException("Member ID cannot be null");
        }
        if (!memberRepository.existsById(memberId)) {
            throw new InvalidArgumentException("Member with ID " + memberId + " does not exist.");
        }
        List<Availability> availabilities = availabilityRepository.findByMember_Id(memberId);
        return availabilities.stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get all availabilities for a specific service by its ID
    public List<AvailabilityResponseDTO> getAvailabilitiesByServiceId(Long serviceId) {
        if (serviceId == null) {
            throw new InvalidArgumentException("Service ID cannot be null");
        }
        if (!serviceRepository.existsById(serviceId)) {
            throw new InvalidArgumentException("Service with ID " + serviceId + " does not exist.");
        }

        List<Long> memberIds = serviceProviderRepository.findMemberIdsByServiceId(serviceId);

        List<AvailabilityResponseDTO> availabilities = new ArrayList<>();
        for (Long memberId : memberIds) {
            availabilities.addAll(getAvailabilitiesByMemberId(memberId));
        }

        return availabilities;
    }

    // Get a specific availability by its ID
    public AvailabilityResponseDTO getAvailabilityById(Long availabilityId) {
        if (availabilityId == null) {
            throw new InvalidArgumentException("Availability ID cannot be null");
        }
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new InvalidArgumentException("Availability with ID " + availabilityId + " does not exist.");
        }
        Availability availability = availabilityRepository.findById(availabilityId).orElse(null);
        return availabilityMapper.toResponseDTO(availability);
    }

    @Transactional
    public AvailabilityResponseDTO createAvailability(AvailabilityRequestDTO requestDTO) {
        Member member = memberRepository.findById(requestDTO.getMemberId())
                .orElseThrow(() -> new InvalidArgumentException("Member not found"));

        Availability availability = availabilityMapper.toEntity(requestDTO);
        availability.setMember(member);
        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTO(saved);
    }


    @Transactional
    public AvailabilityResponseDTO updateAvailability(AvailabilityRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidArgumentException("Availability cannot be null");
        }
        if (!availabilityRepository.existsById(requestDTO.getId())) {
            throw new InvalidArgumentException("Availability with ID " + requestDTO.getId() + " does not exist.");
        }
        Availability availability = availabilityMapper.toEntityWithMember(requestDTO);
        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTO(saved);
    }

    // Delete an availability by its ID
    @Transactional
    public void deleteAvailabilityById(Long availabilityId) {
        if (availabilityId == null) {
            throw new InvalidArgumentException("Availability ID cannot be null");
        }
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new InvalidArgumentException("Availability with ID " + availabilityId + " does not exist.");
        }
        availabilityRepository.deleteById(availabilityId);
    }

    // Get all availabilities
    public List<AvailabilityResponseDTO> getAllAvailabilities() {
        return availabilityRepository.findAll()
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}