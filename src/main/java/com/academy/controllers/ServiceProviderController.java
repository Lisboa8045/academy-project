// ServiceProviderController.java
package com.academy.controllers;

import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.services.ServiceProviderService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {
    private final ServiceProviderService serviceProviderService;

    public ServiceProviderController(ServiceProviderService serviceProviderService) {
        this.serviceProviderService = serviceProviderService;
    }
    @GetMapping
    public List<ServiceProviderResponseDTO> getAllServiceProviders() {
        return serviceProviderService.getAllServiceProviders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderResponseDTO> getServiceProviderById(@PathVariable long id) {
        return serviceProviderService.getServiceProviderById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }

    @PostMapping
    public ServiceProviderResponseDTO createServiceProvider(@Valid @RequestBody ServiceProviderRequestDTO serviceProvider) throws BadRequestException {
        return serviceProviderService.createServiceProviderWithDTO(serviceProvider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceProviderResponseDTO> updateServiceProvider(@PathVariable long id, @Valid @RequestBody ServiceProviderRequestDTO serviceProvider) {
        try {
            ServiceProviderResponseDTO updated = serviceProviderService.updateServiceProvider(id, serviceProvider);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException(ServiceProvider.class, id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceProvider(@PathVariable long id) {
        serviceProviderService.deleteServiceProvider(id);
        return ResponseEntity.noContent().build();
    }
}
