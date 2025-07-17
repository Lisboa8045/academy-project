package com.academy.services;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.academy.dtos.availability.DefaultAvailabilityRequest;
import com.academy.dtos.availability.AvailabilityMapper;
import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Availability;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AvailabilityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final MemberService memberService;
    private final AvailabilityMapper availabilityMapper;
    private final ServiceProviderService serviceProviderService;

    @Autowired
    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            MemberService memberService,
            AvailabilityMapper availabilityMapper, ServiceProviderService serviceProviderService) {
        this.availabilityRepository = availabilityRepository;
        this.memberService = memberService;
        this.availabilityMapper = availabilityMapper;
        this.serviceProviderService = serviceProviderService;
    }

    public List<AvailabilityResponseDTO> getAvailabilitiesByMemberId(Long memberId) {
        validateMemberExists(memberId);
        List<Availability> availabilities = availabilityRepository.findByMember_Id(memberId);
        return mapToResponseDTOs(availabilities);
    }

    public List<AvailabilityResponseDTO> getDefaultAvailabilitiesByMemberId(Long memberId) {
        validateMemberExists(memberId);
        List<Availability> availabilities = availabilityRepository.findByMember_IdAndIsExceptionFalse(memberId);
        return mapToResponseDTOs(availabilities);
    }

    public boolean hasDefaultAvailability(Long memberId) {
        validateMemberExists(memberId);
        return availabilityRepository.existsByMember_IdAndIsExceptionFalse(memberId);
    }

    @Transactional
    public List<AvailabilityResponseDTO> createDefaultAvailability(Long memberId, DefaultAvailabilityRequest request) {
        Member member = memberService.findbyId(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, memberId));

        // Delete existing non-exceptions (default availabilities)
        availabilityRepository.deleteByMember_IdAndIsExceptionFalse(memberId);

        List<Availability> defaultAvailabilities = new ArrayList<>();

        for (DayOfWeek day : request.days()) {
            Availability morning = new Availability();
            morning.setMember(member);
            morning.setDayOfWeek(day);
            morning.setStartDateTime(request.morningStartTime().atDate(LocalDateTime.now().toLocalDate()));
            morning.setEndDateTime(request.morningEndTime().atDate(LocalDateTime.now().toLocalDate()));
            morning.setException(false); // normal slot
            defaultAvailabilities.add(morning);

            if (request.afternoonStartTime() != null && request.afternoonEndTime() != null) {
                Availability afternoon = new Availability();
                afternoon.setMember(member);
                afternoon.setDayOfWeek(day);
                afternoon.setStartDateTime(request.afternoonStartTime().atDate(LocalDateTime.now().toLocalDate()));
                afternoon.setEndDateTime(request.afternoonEndTime().atDate(LocalDateTime.now().toLocalDate()));
                afternoon.setException(false); // normal slot
                defaultAvailabilities.add(afternoon);
            }
        }

        List<Availability> saved = availabilityRepository.saveAll(defaultAvailabilities);
        return mapToResponseDTOs(saved);
    }

    @Transactional
    public AvailabilityResponseDTO createException(Long memberId, AvailabilityRequestDTO requestDTO) {
        validateAvailabilityRequest(requestDTO);
        Member member = validateMemberExists(memberId);

        Availability availability = availabilityMapper.toEntity(requestDTO);
        availability.setMember(member);
        availability.setException(true);

        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTOWithMember(saved);
    }

    public AvailabilityResponseDTO createAvailability(AvailabilityRequestDTO requestDTO) {
        validateAvailabilityRequest(requestDTO);
        Member member = validateMemberExists(requestDTO.memberId());

        Availability availability = availabilityMapper.toEntity(requestDTO);
        availability.setMember(member);
        availability.setException(false);

        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTOWithMember(saved);
    }

    public List<Availability> getAvailabilitiesForProvider(Long providerId) {
        if (providerId == null) {
            throw new InvalidArgumentException("Provider ID cannot be null");
        }

        // First check if the member exists
        memberService.findbyId(providerId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, providerId));

        // Get current date range (next 30 days by default)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(30);

        // Get availabilities for this provider within the date range
        List<Availability> availabilities = availabilityRepository.findByMember_IdAndStartDateTimeBetween(
                providerId,
                now,
                endDate
        );

        return availabilities;
    }

    public AvailabilityResponseDTO updateAvailability(Long availabilityId, AvailabilityRequestDTO requestDTO) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException(Availability.class, availabilityId));

        availability.setDayOfWeek(requestDTO.dayOfWeek());
        availability.setStartDateTime(requestDTO.startDateTime());
        availability.setEndDateTime(requestDTO.endDateTime());

        if (!availability.getMember().getId().equals(requestDTO.memberId())) {
            Member newMember = memberService.findbyId(requestDTO.memberId())
                    .orElseThrow(() -> new EntityNotFoundException(Member.class, requestDTO.memberId()));
            availability.setMember(newMember);
        }

        Availability updated = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDTOWithMember(updated);
    }

    public void deleteAvailabilityById(Long availabilityId) {
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new EntityNotFoundException(Availability.class, availabilityId);
        }
        availabilityRepository.deleteById(availabilityId);
    }

    public List<AvailabilityResponseDTO> getAllAvailabilities() {
        List<Availability> availabilities = availabilityRepository.findAll();
        return mapToResponseDTOs(availabilities);
    }

    private Member validateMemberExists(Long memberId) {
        return memberService.findbyId(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, memberId));
    }

    private void validateAvailabilityRequest(AvailabilityRequestDTO requestDTO) {
        if (!requestDTO.isEndAfterStart()) {
            throw new InvalidArgumentException("EndDateTime must be after StartDateTime");
        }
    }

    private List<AvailabilityResponseDTO> mapToResponseDTOs(List<Availability> availabilities) {
        return availabilities.stream()
                .map(availabilityMapper::toResponseDTOWithMember)
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponseDTO> getAvailabilitiesByServiceId(Long serviceId) {
        if (serviceId == null) {
            throw new InvalidArgumentException("Service ID cannot be null");
        }

        // Get all providers who can serve this service
        List<ServiceProvider> providers = serviceProviderService
                .findProvidersByServiceIdAndPermission(serviceId, ProviderPermissionEnum.SERVE);

        List<AvailabilityResponseDTO> result = new ArrayList<>();

        for (ServiceProvider provider : providers) {
            Long providerId = provider.getProvider().getId();
            // Get all availabilities for this provider
            List<Availability> providerAvailabilities = availabilityRepository.findByMember_Id(providerId);
            // Convert to DTOs and add to result
            result.addAll(providerAvailabilities.stream()
                    .map(availabilityMapper::toResponseDTOWithMember)
                    .collect(Collectors.toList()));
        }

        return result;
    }
}