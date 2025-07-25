    // ServiceProviderController.java
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
        public ResponseEntity<List<ServiceProviderResponseDTO>> getAllServiceProviders() {
            List<ServiceProviderResponseDTO> serviceProviders = serviceProviderService.getAllServiceProviders();
            return ResponseEntity.ok(serviceProviders);
        }

        @GetMapping("/services/{serviceId}")
        public ResponseEntity<List<ServiceProviderResponseDTO>> getServiceProvidersByServiceId(@PathVariable long serviceId) {
            List<ServiceProviderResponseDTO> serviceProviders = serviceProviderService.getServiceProvidersByServiceId(serviceId);
            return ResponseEntity.ok(serviceProviders);
        }

        @GetMapping("/{id}")
        public ResponseEntity<ServiceProviderResponseDTO> getServiceProviderById(@PathVariable long id) {
            return serviceProviderService.getServiceProviderById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
        }

        @PostMapping
        public ResponseEntity<ServiceProviderResponseDTO> createServiceProvider(
                @Valid @RequestBody ServiceProviderRequestDTO serviceProvider) throws BadRequestException {

            ServiceProviderResponseDTO createdServiceProvider = serviceProviderService.createServiceProviderWithDTO(serviceProvider);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdServiceProvider);
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
        public ResponseEntity<Void> deleteServiceProvider(@PathVariable long id) throws BadRequestException {
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
