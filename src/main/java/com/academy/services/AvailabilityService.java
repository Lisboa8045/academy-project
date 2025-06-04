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
import com.academy.exceptions.EntityNotFoundException;
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

    // Create a new availability
    @Transactional
    public AvailabilityResponseDTO createAvailability(AvailabilityRequestDTO requestDTO) {
        Long memberId = requestDTO.memberId();
        if (memberId == null || memberId <= 0) {
            throw new InvalidArgumentException("Member ID must be positive, non-zero and non-null.");
        }
        if (requestDTO.dayOfWeek() == null) {
            throw new InvalidArgumentException("DayOfWeek cannot be null.");
        }
        if (requestDTO.startDateTime() == null || requestDTO.endDateTime() == null) {
            throw new InvalidArgumentException("StartDateTime and EndDateTime cannot be null.");
        }
        if (!requestDTO.startDateTime().isBefore(requestDTO.endDateTime())) {
            throw new InvalidArgumentException("StartDateTime must be before EndDateTime.");
        }

        Member member = memberService.findbyId(memberId)
                .orElseThrow(() -> new InvalidArgumentException("Member not found"));

        boolean overlaps = availabilityRepository.existsByMember_IdAndDayOfWeekAndTimeOverlap(
                memberId,
                requestDTO.dayOfWeek(),
                requestDTO.startDateTime(),
                requestDTO.endDateTime()
        );

        if (overlaps) {
            throw new InvalidArgumentException("Availability overlaps with existing availability for this member.");
        }

        Availability availability = availabilityMapper.toEntity(requestDTO);
        availability.setMember(member);
        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTOWithMember(saved);
    }

    // Update an existing availability
    @Transactional
    public AvailabilityResponseDTO updateAvailability(AvailabilityRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidArgumentException("Availability cannot be null");
        }
        if (!availabilityRepository.existsById(requestDTO.id())) {
            throw new InvalidArgumentException("Availability with ID " + requestDTO.id() + " does not exist.");
        }
        Availability availability = availabilityMapper.toEntityWithMember(requestDTO);
        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTOWithMember(saved);
    }

    @Transactional
    public void deleteAvailabilityById(Long availabilityId) {
        if (availabilityId == null) {
            throw new InvalidArgumentException("Availability ID cannot be null");
        }
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new EntityNotFoundException(Availability.class, " with ID " + availabilityId + " does not exist.");
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