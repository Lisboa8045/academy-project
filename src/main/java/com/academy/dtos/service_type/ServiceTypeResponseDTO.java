package com.academy.dtos.service_type;

import java.time.LocalDateTime;

public record ServiceTypeResponseDTO (
    Long id,
    String name,
    String icon,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
