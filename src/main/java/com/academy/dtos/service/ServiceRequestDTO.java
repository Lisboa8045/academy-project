package com.academy.dtos.service;

import jakarta.validation.constraints.*;

import java.util.List;

public record ServiceRequestDTO(
        @NotBlank String name,
        @NotBlank String description,
        @Positive double price,
        @Min(value = 0, message = "cannot be less than 0%")
        @Max(value = 100, message = "cannot exceed 100%")
        int discount,
        boolean negotiable,
        @Positive int duration,
        @NotNull Long serviceTypeId,
        List<String> tagNames
) {}