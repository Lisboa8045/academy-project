package com.academy.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.academy.models.member.Member;
import com.academy.models.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.academy.dtos.availability.AvailabilityMapper;
import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Availability;
import com.academy.repositories.AvailabilityRepository;
import jakarta.transaction.Transactional;

@org.springframework.stereotype.Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ServiceProviderService serviceProviderService;
    private final ServiceService serviceService;
    private final MemberService memberService;
    private final AvailabilityMapper availabilityMapper;

    @Value("${slot.window.days:30}")
    private int slotWindowDays;


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
            throw new EntityNotFoundException(Member.class, " with ID " + memberId + " does not exist.");
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
            throw new EntityNotFoundException(Service.class, " with ID " + serviceId + " does not exist.");
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
            throw new EntityNotFoundException(Availability.class, " with ID " + availabilityId + " does not exist.");
        }
        Availability availability = availabilityRepository.findById(availabilityId).orElse(null);
        return availabilityMapper.toResponseDTOWithMember(availability);
    }

    // Create a new availability
    @Transactional
    public AvailabilityResponseDTO createAvailability(AvailabilityRequestDTO requestDTO) {

        if (!requestDTO.isEndAfterStart()) {
            throw new InvalidArgumentException("EndDateTime must be after StartDateTime");
        }

        Long memberId = requestDTO.memberId();
        Member member = memberService.findbyId(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, " with ID " + memberId + " not found."));

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
    public AvailabilityResponseDTO updateAvailability(Long availabilityId, AvailabilityRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidArgumentException("Availability cannot be null");
        }

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException(Availability.class, " with ID " + availabilityId + " does not exist."));

        // Atualiza campos simples
        availability.setDayOfWeek(requestDTO.dayOfWeek());
        availability.setStartDateTime(requestDTO.startDateTime());
        availability.setEndDateTime(requestDTO.endDateTime());

        // Verifica se é necessário atualizar o membro associado
        if (availability.getMember().getId() != requestDTO.memberId()) {
            Member newMember = memberService.findbyId(requestDTO.memberId())
                    .orElseThrow(() -> new EntityNotFoundException(Member.class, " with ID " + requestDTO.memberId() + " not found."));
            availability.setMember(newMember);
        }

        return availabilityMapper.toResponseDTOWithMember(availability);
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

    public List<Availability> getAvailabilitiesForProvider(Long providerId) {
        if (providerId == null) {
            throw new InvalidArgumentException("Provider ID cannot be null");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime days = now.plusDays(slotWindowDays);
        return availabilityRepository.findByMember_IdAndStartDateTimeBetween(providerId, now, days);
    }
}