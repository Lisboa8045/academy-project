package com.academy.services;

import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.ProviderPermissionRepository;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProviderPermissionService {

    private final ProviderPermissionRepository providerPermissionRepository;

    @Autowired
    public ProviderPermissionService(ProviderPermissionRepository providerPermissionRepository) {
        this.providerPermissionRepository = providerPermissionRepository;
    }
    public boolean hasPermission(Long serviceProviderId, String permission){
        return providerPermissionRepository.existsByServiceProviderIdAndPermission(serviceProviderId, permission);
    }
    public List<ProviderPermissionEnum> getPermissions(Long serviceProviderId){
        return providerPermissionRepository.findAllByServiceProviderId(serviceProviderId).stream()
                .map(ProviderPermission::getPermission).toList();
    }
    public ServiceProvider createPermissionsViaList(List<ProviderPermissionEnum> permissions, ServiceProvider serviceProvider){
        List<ProviderPermission> providerPermissions = new ArrayList<>();
        for(ProviderPermissionEnum permission : permissions){
            ProviderPermission providerPermission = new ProviderPermission();
            providerPermission.setPermission(permission);
            providerPermission.setServiceProvider(serviceProvider);
            providerPermissionRepository.save(providerPermission);

            providerPermissions.add(providerPermission);
        }
        serviceProvider.setPermissions(providerPermissions);
        return serviceProvider;
    }

    @Transactional
    public void deletePermissionsFromServiceProvider(ServiceProvider serviceProvider) {
        List<ProviderPermission> permissionsToDelete = new ArrayList<>(serviceProvider.getPermissions());

        for (ProviderPermission permission : permissionsToDelete) {
            permission.setServiceProvider(null);
            serviceProvider.getPermissions().remove(permission);

            providerPermissionRepository.delete(permission);
        }
    }
    public void deleteAllByServiceProvider(ServiceProvider serviceProvider) {
        providerPermissionRepository.deleteAll(serviceProvider.getPermissions());
    }
}
