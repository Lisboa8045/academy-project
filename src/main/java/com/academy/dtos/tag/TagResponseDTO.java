package com.academy.dtos.tag;

import java.time.LocalDateTime;
import java.util.List;

public record TagResponseDTO(
        Long id,
        String name,
        Boolean isCustom,
        List<Long> serviceIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}