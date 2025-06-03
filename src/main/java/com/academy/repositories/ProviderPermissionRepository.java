package com.academy.repositories;

import com.academy.models.service.service_provider.ProviderPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderPermissionRepository extends JpaRepository<ProviderPermission,Long> {

    boolean existsByServiceProviderIdAndPermission(Long serviceProviderId, String permission);
    List<ProviderPermission> findAllByServiceProviderId(Long serviceProviderId);

}

