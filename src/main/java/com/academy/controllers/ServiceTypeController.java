package com.academy.controllers;

import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.services.ServiceTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service-types")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @Autowired
    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ServiceTypeResponseDTO> create(@Valid @RequestBody ServiceTypeRequestDTO dto) {
        ServiceTypeResponseDTO response = serviceTypeService.create(dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceTypeResponseDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody ServiceTypeRequestDTO dto) {
        ServiceTypeResponseDTO response = serviceTypeService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceTypeResponseDTO>> getAll() {
        List<ServiceTypeResponseDTO> responses = serviceTypeService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceTypeResponseDTO> getById(@PathVariable Long id) {
        ServiceTypeResponseDTO response = serviceTypeService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}