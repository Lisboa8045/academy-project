// ServiceProviderController.java
package com.academy.controllers;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.service_provider.ServiceProvider;
import com.academy.services.ServiceProviderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {
    private ServiceProviderService serviceProviderService;

    public ServiceProviderController(ServiceProviderService serviceProviderService) {
        this.serviceProviderService = serviceProviderService;
    }
    @GetMapping
    public List<ServiceProvider> getAllServiceProviders() {
        return serviceProviderService.getAllServiceProviders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceProvider> getServiceProviderById(@PathVariable long id) {
        return serviceProviderService.getServiceProviderById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }

    @PostMapping
    public ServiceProvider createServiceProvider(@Valid @RequestBody ServiceProvider serviceProvider) {
        return serviceProviderService.createServiceProvider(serviceProvider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceProvider> updateServiceProvider(@PathVariable long id, @Valid @RequestBody ServiceProvider serviceProvider) {
        try {
            ServiceProvider updated = serviceProviderService.updateServiceProvider(id, serviceProvider);
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
