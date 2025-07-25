package com.academy.security;

import com.academy.services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceSecurity {

    private final ServiceService serviceService;

    @Autowired
    public ServiceSecurity(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    public boolean isOwner(Long serviceId, String username) {
        return serviceService.isServiceOwnedByUser(serviceId, username);
    }
}