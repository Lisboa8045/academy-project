package com.academy.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.dtos.availability.AvailabilityMapper;
import com.academy.models.Availability;
import com.academy.services.AvailabilityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public AvailabilityController(AvailabilityService availabilityService, AvailabilityMapper availabilityMapper) {
        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    // Get all availabilities
    @GetMapping("/availability")
    public List<AvailabilityResponseDTO> getAllAvailabilities() {
        return availabilityService.getAllAvailabilities()
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get availabilities by memberId
    @GetMapping("/availability/member/{memberId}")
    public List<AvailabilityResponseDTO> getMemberAvailability(@PathVariable long memberId) {
        return availabilityService.getAvailabilitiesByMemberId(memberId)
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get availabilities by serviceId
    @GetMapping("/availability/service/{serviceId}")
    public List<AvailabilityResponseDTO> getServiceAvailability(@PathVariable long serviceId) {
        return availabilityService.getAvailabilitiesByServiceId(serviceId)
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Create a new availability
    @PostMapping("/availability")
    public AvailabilityResponseDTO createAvailability(@Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
        Availability availability = availabilityMapper.toEntityWithMember(availabilityRequestDTO);
        Availability createdAvailability = availabilityService.createAvailability(availability);
        return availabilityMapper.toResponseDTO(createdAvailability);
    }

    // Update an existing availability
    @PutMapping("/availability/{availabilityId}")
    public AvailabilityResponseDTO updateAvailability(@Valid @PathVariable long availabilityId,
            @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
        Availability availability = availabilityMapper.toEntityWithMember(availabilityRequestDTO);
        Availability updatedAvailability = availabilityService.updateAvailability(availabilityId, availability);
        return availabilityMapper.toResponseDTO(updatedAvailability);
    }


    // Delete an availability
    @DeleteMapping("/availability/{availabilityId}")
    public void deleteAvailability(@PathVariable long availabilityId) {
        availabilityService.deleteAvailabilityById(availabilityId);
    }


    
}