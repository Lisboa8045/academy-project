package com.academy.controllers;

import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.services.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/services")
public class ServiceController {

    private ServiceService serviceService;

    @Autowired
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping("/{ownerId}")
    public ResponseEntity<ServiceResponseDTO> create(@PathVariable Long ownerId,
                                                     @Valid @RequestBody ServiceRequestDTO dto) {
        return ResponseEntity.ok(serviceService.create(dto, ownerId));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> update(@PathVariable Long serviceId,
                                                     @Valid @RequestBody ServiceRequestDTO dto) {
        ServiceResponseDTO response = serviceService.update(serviceId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> getAll() {
        List<ServiceResponseDTO> responses = serviceService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> getById(@PathVariable Long serviceId) {
        ServiceResponseDTO response = serviceService.getById(serviceId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> delete(@PathVariable Long serviceId) {
        serviceService.delete(serviceId);
        return ResponseEntity.noContent().build();
    }
}
