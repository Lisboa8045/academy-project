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
    private final ServiceProviderService serviceProviderService;
    private final ServiceService serviceService;
    private final MemberService memberService;
    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            ServiceProviderService serviceProviderService,
            MemberService memberService,
            ServiceService serviceService,
            AvailabilityMapper availabilityMapper) {

        this.availabilityRepository = availabilityRepository;
        this.serviceProviderService = serviceProviderService;
        this.memberService = memberService;
        this.serviceService = serviceService;
        this.availabilityMapper = availabilityMapper;
    }

    // Get all availabilities for a specific member by their ID
    public List<AvailabilityResponseDTO> getAvailabilitiesByMemberId(Long memberId) {
        if (memberId == null) {
            throw new InvalidArgumentException("Member ID cannot be null");
        }
        if (!memberService.existsById(memberId)) {
            throw new InvalidArgumentException("Member with ID " + memberId + " does not exist.");
        }
        List<Availability> availabilities = availabilityRepository.findByMember_Id(memberId);
        return availabilities.stream()
                .map(availabilityMapper::toResponseDTOWithMember)
                .collect(Collectors.toList());
    }

    // Get all availabilities for a specific service by its ID
    public List<AvailabilityResponseDTO> getAvailabilitiesByServiceId(Long serviceId) {
        if (serviceId == null) {
            throw new InvalidArgumentException("Service ID cannot be null");
        }
        if (!serviceService.existsById(serviceId)) {
            throw new InvalidArgumentException("Service with ID " + serviceId + " does not exist.");
        }

        List<Long> memberIds = serviceProviderService.findMemberIdsByServiceId(serviceId);

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
        return availabilityMapper.toResponseDTOWithMember(availability);
    }

    @Transactional
    public AvailabilityResponseDTO createAvailability(AvailabilityRequestDTO requestDTO) {
        
        Member member = memberService.findbyId(requestDTO.getMemberId())
                .orElseThrow(() -> new InvalidArgumentException("Member not found"));

        boolean exists = availabilityRepository
        .findByMember_IdAndDayOfWeekAndStartDateTimeAndEndDateTime(
            requestDTO.getMemberId(),
            requestDTO.getDayOfWeek(),
            requestDTO.getStartDateTime(),
            requestDTO.getEndDateTime()
        ).isPresent();

        if (exists) {
            throw new InvalidArgumentException("Availability already exists for this member at the specified time and day.");
        }

        Availability availability = availabilityMapper.toEntity(requestDTO);
        availability.setMember(member);
        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTOWithMember(saved);
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
        return availabilityMapper.toResponseDTOWithMember(saved);
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
                .map(availabilityMapper::toResponseDTOWithMember)
                .collect(Collectors.toList());
    }
}