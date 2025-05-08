package com.academy.repositories;

import com.academy.models.service_provider.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {

    @Query("SELECT sp.member.id FROM ServiceProvider sp WHERE sp.service.id = :serviceId")
    List<Long> findMemberIdsByServiceId(Long serviceId);
}