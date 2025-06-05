package com.academy.repositories;

import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {

    @Query("SELECT sp.provider.id FROM ServiceProvider sp WHERE sp.service.id = :serviceId")
    List<Long> findMemberIdsByServiceId(Long serviceId);
    Optional<ServiceProvider> findByProviderUsernameAndServiceId(String username, Long serviceId);
    
    List<ServiceProvider> service(Service service);

    boolean existsByServiceId(Long serviceId);

    boolean existsByServiceIdAndProviderUsername(Long serviceId, String username);
}