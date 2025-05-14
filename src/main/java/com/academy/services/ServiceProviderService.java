// ServiceProviderService.java
package com.academy.services;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.service_provider.ServiceProvider;
import com.academy.repositories.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceProviderService {

    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<ServiceProvider> getAllServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    public Optional<ServiceProvider> getServiceProviderById(long id) {
        return serviceProviderRepository.findById(id);
    }

    public ServiceProvider createServiceProvider(ServiceProvider serviceProvider) {
        return serviceProviderRepository.save(serviceProvider);
    }

    public ServiceProvider updateServiceProvider(long id, ServiceProvider details) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));

        serviceProvider.setProvider(details.getProvider());
        serviceProvider.setService(details.getService());
        serviceProvider.setPermission(details.getPermission());
        serviceProvider.setAppointmentList(details.getAppointmentList());

        return serviceProviderRepository.save(serviceProvider);
    }

    public void deleteServiceProvider(long id) {
        serviceProviderRepository.deleteById(id);
    }
}
