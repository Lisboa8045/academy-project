package com.academy.repositories;

import com.academy.models.service.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceImageRepository extends JpaRepository<ServiceImage, Long> {
    void deleteServiceImageByServiceId(Long serviceId);
}
