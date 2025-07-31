package com.academy.controllers;

import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.services.ServiceProviderService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/service-providers")
public class ServiceProviderController {
    private final ServiceProviderService serviceProviderService;

    public ServiceProviderController(ServiceProviderService serviceProviderService) {
        this.serviceProviderService = serviceProviderService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceProviderResponseDTO>> getAllServiceProviders() {
        List<ServiceProviderResponseDTO> serviceProviders = serviceProviderService.getAllServiceProviders();
        return ResponseEntity.ok(serviceProviders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderResponseDTO> getServiceProviderById(@PathVariable long id) {
        return serviceProviderService.getServiceProviderById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('WORKER')")
    @PostMapping
    public ResponseEntity<ServiceProviderResponseDTO> createServiceProvider(
            @Valid @RequestBody ServiceProviderRequestDTO serviceProvider) throws BadRequestException {

        ServiceProviderResponseDTO createdServiceProvider = serviceProviderService.createServiceProviderWithDTO(serviceProvider);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdServiceProvider);
    }

    @PreAuthorize("hasRole('ADMIN') or @serviceProviderSecurity.isSelf(#id, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceProviderResponseDTO> updateServiceProvider(@PathVariable long id, @Valid @RequestBody ServiceProviderRequestDTO serviceProvider) {
        try {
            ServiceProviderResponseDTO updated = serviceProviderService.updateServiceProvider(id, serviceProvider);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException(ServiceProvider.class, id);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @serviceProviderSecurity.isSelf(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceProvider(@PathVariable long id) {
        serviceProviderService.deleteServiceProvider(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/{serviceId}/providers/{providerId}")
    public ResponseEntity<ServiceProviderResponseDTO> getServiceProviderIdByServiceAndProvider(
            @PathVariable Long serviceId,
            @PathVariable Long providerId) {

        ServiceProviderResponseDTO serviceProvider = serviceProviderService.getServiceProviderDTOByProviderIdAndServiceID(providerId, serviceId);

        return ResponseEntity.ok(serviceProvider);
    }

}
