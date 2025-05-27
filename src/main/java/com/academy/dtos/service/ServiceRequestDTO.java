package com.academy.dtos.service;

import com.academy.models.ServiceType;
import jakarta.validation.constraints.*;
import java.util.List;

public record ServiceRequestDTO(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull Long ownerId,
        @Positive double price,
        @Min(0) @Max(100) int discount,
        boolean negotiable,
        @Positive int duration,
        @NotNull ServiceType serviceType,
        List<String> tagNames
) {}
