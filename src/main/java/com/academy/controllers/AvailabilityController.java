package com.academy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.services.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Autowired
    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDTO>> getAllAvailabilities() {
        List<AvailabilityResponseDTO> response = availabilityService.getAllAvailabilities();
        return ResponseEntity.ok(response); 
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getMemberAvailability(@PathVariable long memberId) {
        List<AvailabilityResponseDTO> response = availabilityService.getAvailabilitiesByMemberId(memberId);
        return ResponseEntity.ok(response); 
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getServiceAvailability(@PathVariable long serviceId) {
        List<AvailabilityResponseDTO> response = availabilityService.getAvailabilitiesByServiceId(serviceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDTO> createAvailability(@Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
        AvailabilityResponseDTO response = availabilityService.createAvailability(availabilityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN') or @availabilitySecurity.isOwner(#availabilityId, authentication.name)")
    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponseDTO> updateAvailability(
            @PathVariable long availabilityId,
            @Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {

        AvailabilityResponseDTO response = availabilityService.updateAvailability(availabilityId, availabilityRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or @availabilitySecurity.isOwner(#availabilityId, authentication.name)")
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable long availabilityId) {
        availabilityService.deleteAvailabilityById(availabilityId);
        return ResponseEntity.noContent().build(); 
    }
}