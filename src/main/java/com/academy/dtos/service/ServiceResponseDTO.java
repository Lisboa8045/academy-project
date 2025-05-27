package com.academy.dtos.service;

import java.time.LocalDateTime;
import java.util.List;

public record ServiceResponseDTO(
        Long id,
        String name,
        String description,
        double price,
        int discount,
        boolean negotiable,
        int duration,
        String serviceTypeName,
        List<String> tagNames,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}