package com.academy.dtos.service;

import com.academy.models.ServiceType;

import java.util.List;

public record ServiceResponseDTO(
        Long id,
        String name,
        String description,
        long ownerId,
        double price,
        int discount,
        boolean negotiable,
        int duration,
        ServiceType serviceType,
        List<String> tagNames
) {}
