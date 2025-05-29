package com.academy.dtos.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TagRequestDTO(
        @NotBlank String name,
        @NotNull Boolean isCustom,
        List<Long> serviceIds
) {}