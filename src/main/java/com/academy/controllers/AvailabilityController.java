package com.academy.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.dtos.availability.AvailabilityMapper;
import com.academy.models.Availability;
import com.academy.services.AvailabilityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public AvailabilityController(AvailabilityService availabilityService, AvailabilityMapper availabilityMapper) {
        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    // Get all availabilities
    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDTO>> getAllAvailabilities() {
        List<AvailabilityResponseDTO> response = availabilityService.getAllAvailabilities()
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response); 
    }

    // Get availabilities by memberId
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getMemberAvailability(@PathVariable long memberId) {
        List<AvailabilityResponseDTO> response = availabilityService.getAvailabilitiesByMemberId(memberId)
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response); 
    }

    // Get availabilities by serviceId
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getServiceAvailability(@PathVariable long serviceId) {
        List<AvailabilityResponseDTO> response = availabilityService.getAvailabilitiesByServiceId(serviceId)
                .stream()
                .map(availabilityMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Create a new availability
    @PostMapping
    public ResponseEntity<AvailabilityResponseDTO> createAvailability(@Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
        Availability availability = availabilityMapper.toEntityWithMember(availabilityRequestDTO);
        Availability createdAvailability = availabilityService.createAvailability(availability);
        AvailabilityResponseDTO response = availabilityMapper.toResponseDTO(createdAvailability);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update an existing availability
    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponseDTO> updateAvailability(@Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
        Availability availability = availabilityMapper.toEntityWithMember(availabilityRequestDTO);
        Availability updatedAvailability = availabilityService.updateAvailability(availability);
        AvailabilityResponseDTO response = availabilityMapper.toResponseDTO(updatedAvailability);
        return ResponseEntity.ok(response); 
    }

    // Delete an availability
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable long availabilityId) {
        availabilityService.deleteAvailabilityById(availabilityId);
        return ResponseEntity.noContent().build(); 
    }
}