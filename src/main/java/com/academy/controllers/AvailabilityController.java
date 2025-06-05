package com.academy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.services.AvailabilityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Autowired
    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // Get all availabilities
    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDTO>> getAllAvailabilities() {
        List<AvailabilityResponseDTO> response = availabilityService.getAllAvailabilities();
        return ResponseEntity.ok(response); 
    }

    // Get availabilities by memberId
    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getMemberAvailability(@PathVariable long memberId) {
        List<AvailabilityResponseDTO> response = availabilityService.getAvailabilitiesByMemberId(memberId);
        return ResponseEntity.ok(response); 
    }

    // Get availabilities by serviceId
    @GetMapping("/services/{serviceId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getServiceAvailability(@PathVariable long serviceId) {
        List<AvailabilityResponseDTO> response = availabilityService.getAvailabilitiesByServiceId(serviceId);
        return ResponseEntity.ok(response);
    }

    // Create a new availability
    @PostMapping
    public ResponseEntity<AvailabilityResponseDTO> createAvailability(@Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
        AvailabilityResponseDTO response = availabilityService.createAvailability(availabilityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponseDTO> updateAvailability(
            @PathVariable long availabilityId,
            @Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {

        AvailabilityResponseDTO response = availabilityService.updateAvailability(availabilityId, availabilityRequestDTO);
        return ResponseEntity.ok(response);
    }

    // Delete an availability
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable long availabilityId) {
        availabilityService.deleteAvailabilityById(availabilityId);
        return ResponseEntity.noContent().build(); 
    }
}