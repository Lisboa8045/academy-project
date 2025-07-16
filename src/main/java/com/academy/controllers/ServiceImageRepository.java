package com.academy.controllers;

import com.academy.models.service.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;

interface ServiceImageRepository extends JpaRepository<ServiceImage, Long> {
}
