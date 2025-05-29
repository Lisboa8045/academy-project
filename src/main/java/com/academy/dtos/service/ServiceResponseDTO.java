package com.academy.dtos.service;

import com.academy.models.service.ServiceTypeEnum;
import com.academy.models.service.service_provider.ProviderPermissionEnum;

import java.time.LocalDateTime;
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
        List<ProviderPermissionEnum> permissions,
        ServiceTypeEnum serviceType,
        List<String> tagNames,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
