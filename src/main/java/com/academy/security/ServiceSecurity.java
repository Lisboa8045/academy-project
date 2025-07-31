package com.academy.security;

import com.academy.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceSecurity {

    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceSecurity(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public boolean isOwner(Long serviceId, String username) {
        return serviceRepository.findById(serviceId)
                .map(service -> service.getOwner().getUsername().equals(username))
                .orElse(false);
    }
}
