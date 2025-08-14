package com.academy.controllers;

import com.academy.dtos.availability.AvailabilityRequestNewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.academy.services.AvailabilityService;

@RestController
@RequestMapping("/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Autowired
    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/create-availabilities")
    public ResponseEntity<String> createAvailabilities( @RequestBody AvailabilityRequestNewDTO request){
        availabilityService.createAvailabilities(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/member")
    public ResponseEntity<AvailabilityRequestNewDTO> getMemberAvailability() {
        return ResponseEntity.ok(availabilityService.getMemberAvailability());
    }
    }