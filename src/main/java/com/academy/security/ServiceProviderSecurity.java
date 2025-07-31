package com.academy.security;

import com.academy.repositories.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceProviderSecurity {

    private final ServiceProviderRepository serviceProviderRepository;

    @Autowired
    public ServiceProviderSecurity(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public boolean isSelf(Long serviceProviderId, String username) {
        return serviceProviderRepository.findById(serviceProviderId)
                .map(sp -> sp.getProvider().getUsername().equals(username))
                .orElse(false);
    }
}
