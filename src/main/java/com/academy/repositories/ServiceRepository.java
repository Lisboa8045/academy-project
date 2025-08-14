package com.academy.repositories;

import com.academy.models.service.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {

    @Query("SELECT DISTINCT s FROM Service s " +
            "LEFT JOIN s.serviceProviders sp " +
            "WHERE s.enabled = true AND( s.owner.id = :memberId OR sp.provider.id = :memberId)")
    Page<Service> queryEnabledServicesByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Service s " +
            "LEFT JOIN s.serviceProviders sp " +
            "WHERE s.enabled = false AND( s.owner.id = :memberId OR sp.provider.id = :memberId)")
    Page<Service> queryNotEnabledServicesByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Service s " +
            "LEFT JOIN s.serviceProviders sp " +
            "WHERE s.status != 'REJECTED' AND (s.owner.id = :memberId OR sp.provider.id = :memberId)")
    Page<Service> queryNotRejectedServicesByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
