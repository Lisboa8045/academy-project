package com.academy.dtos.service;

import com.academy.models.service.ServiceTypeEnum;
import jakarta.validation.constraints.*;
import java.util.List;

public record ServiceRequestDTO(
        @NotBlank String name,
        @NotBlank String description,
        @Positive double price,
        @Min(0) @Max(100) int discount,
        boolean negotiable,
        @Positive int duration,
        @NotNull ServiceTypeEnum serviceType,
        List<String> tagNames
) {}
