package com.academy.dtos.service_type;

import jakarta.validation.constraints.NotBlank;

public record ServiceTypeRequestDTO (
    @NotBlank String name,
    @NotBlank String icon
) {}
