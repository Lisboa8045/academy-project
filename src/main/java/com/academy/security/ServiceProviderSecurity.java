package com.academy.security;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
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

    public boolean canUpdateServiceProvider(long serviceProviderId, String username) {
        if (isSelf(serviceProviderId, username)) {
            return true;
        }

        ServiceProvider targetSP = serviceProviderRepository.findById(serviceProviderId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, serviceProviderId));

        Long serviceId = targetSP.getService().getId();

        return serviceProviderRepository.existsByProvider_UsernameAndService_IdAndPermissions_Permission(
                username, serviceId, ProviderPermissionEnum.UPDATE_PERMISSIONS);
    }

    public boolean isSelf(Long serviceProviderId, String username) {
        return serviceProviderRepository.findById(serviceProviderId)
                .map(sp -> sp.getProvider().getUsername().equals(username))
                .orElse(false);
    }
}
