package com.academy.repositories;

import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Optional<ServiceProvider> findByProviderUsername(String username);

    List<ServiceProvider> findAllByProviderId(Long providerId);

    @Query("SELECT sp.provider.id FROM ServiceProvider sp WHERE sp.service.id = :serviceId")
    List<Long> findMemberIdsByServiceId(Long serviceId);
    Optional<ServiceProvider> findByProviderUsernameAndServiceId(String username, Long serviceId);

    Optional<ServiceProvider> findByProviderIdAndServiceId(Long id, Long serviceId);
    List<ServiceProvider> service(Service service);

    boolean existsByServiceId(Long serviceId);

    boolean existsByServiceIdAndProviderUsername(Long serviceId, String username);

    boolean existsByServiceIdAndProviderId(Long serviceId, Long id);

    Optional<ServiceProvider> findByServiceIdAndProviderId(Long serviceId, Long providerId);

    @Query("SELECT sp FROM ServiceProvider sp JOIN sp.permissions p WHERE sp.service.id = :serviceId AND p.permission = :permission")
    List<ServiceProvider> findProvidersByServiceIdAndPermission(Long serviceId, ProviderPermissionEnum permission);

    boolean existsByServiceIdAndPermissions_Permission(Long id, ProviderPermissionEnum providerPermissionEnum);

    @Query("SELECT AVG(sp.rating) FROM ServiceProvider sp WHERE sp.service.id = :serviceId")
    Double findAverageRatingByService_Id(@Param("serviceId") Long serviceId);

    @Query("SELECT AVG(sp.rating) FROM ServiceProvider sp WHERE sp.provider.id = :memberId")
    Double findAverageRatingByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("UPDATE ServiceProvider sp SET sp.provider = NULL WHERE sp.provider.id = :memberId")
    void unlinkByMemberId(@Param("memberId") Long memberId);
}