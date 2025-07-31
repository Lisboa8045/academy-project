package com.academy.controllers;

import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service.UpdatePermissionsRequestDto;
import com.academy.exceptions.AuthenticationException;
import com.academy.services.ServiceService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("services")
public class ServiceController {

    private final ServiceService serviceService;

    @Autowired
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('WORKER')")
    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(@Valid @RequestBody ServiceRequestDTO dto) throws AuthenticationException, BadRequestException {
        return ResponseEntity.ok(serviceService.create(dto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('WORKER')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody ServiceRequestDTO dto) {
        ServiceResponseDTO response = serviceService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> getAllServices() {
        List<ServiceResponseDTO> responses = serviceService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> getById(@PathVariable Long id) {
        ServiceResponseDTO response = serviceService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ServiceResponseDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) Boolean negotiable,
            @RequestParam(required = false) String serviceTypeName,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ServiceResponseDTO> responses = serviceService.searchServices(name, minPrice,
                maxPrice, minDuration, maxDuration, negotiable, serviceTypeName, pageable);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("@serviceSecurity.isOwner(#id, authentication.name) or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableService(@PathVariable Long id) {
        serviceService.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('WORKER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> updatePermissions(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionsRequestDto request) throws AuthenticationException, BadRequestException {
        return ResponseEntity.ok(serviceService.updateMemberPermissions(id, request.memberId(), request.permissions()));
    }

    @GetMapping("/my-services/{id}")
    public ResponseEntity<Page<ServiceResponseDTO>> getMyServices(@PathVariable Long id, Pageable pageable){
        return ResponseEntity.ok(serviceService.getServicesByMemberId(id, pageable));
    }
}
