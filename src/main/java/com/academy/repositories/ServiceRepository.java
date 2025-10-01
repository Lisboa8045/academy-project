package com.academy.repositories;

import com.academy.models.service.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query("""
        select distinct s
        from Service s
        join s.serviceProviders sp
        where sp.provider.id = :memberId
          and s.owner.id = :memberId
    """)
    List<Service> findOwnedAndProvidedByMember(@Param("memberId") Long memberId);

    List<Service> findTop10ByEnabledTrueOrderByRatingDesc();

    List<Service> findTop10ByEnabledTrueAndDiscountGreaterThanOrderByDiscountDesc(double discountThreshold);

    @Query("SELECT s FROM Service s " +
            "LEFT JOIN s.serviceProviders sp " +
            "LEFT JOIN sp.appointmentList a " +
            "WHERE s.enabled = true " +
            "AND a.endDateTime >= :startDate " +
            "GROUP BY s " +
            "ORDER BY COUNT(a) DESC")
    List<Service> findTopTrendingServicesInPastMonth(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}
