package com.academy.controllers;

import java.util.List;

import com.academy.exceptions.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.academy.dtos.availability.DefaultAvailabilityRequest;
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

    @GetMapping("/members/{memberId}/default")
    public ResponseEntity<List<AvailabilityResponseDTO>> getMemberDefaultAvailability(@PathVariable long memberId) {
        List<AvailabilityResponseDTO> response = availabilityService.getDefaultAvailabilitiesByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/{memberId}/has-default")
    public ResponseEntity<Boolean> hasDefaultAvailability(@PathVariable long memberId) {
        boolean hasDefault = availabilityService.hasDefaultAvailability(memberId);
        return ResponseEntity.ok(hasDefault);
    }

    @PostMapping("/members/{memberId}/default")
    public ResponseEntity<?> createDefaultAvailability(
            @PathVariable long memberId,
            @RequestBody @Valid DefaultAvailabilityRequest request) {

        try {
            List<AvailabilityResponseDTO> response = availabilityService.createDefaultAvailability(memberId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/members/{memberId}/exceptions")
    public ResponseEntity<AvailabilityResponseDTO> createException(
            @PathVariable long memberId,
            @Valid @RequestBody AvailabilityRequestDTO requestDTO) {
        AvailabilityResponseDTO response = availabilityService.createException(memberId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDTO> createAvailability(
            @Valid @RequestBody AvailabilityRequestDTO availabilityRequestDTO) {
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

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable long availabilityId) {
        availabilityService.deleteAvailabilityById(availabilityId);
        return ResponseEntity.noContent().build();
    }
}