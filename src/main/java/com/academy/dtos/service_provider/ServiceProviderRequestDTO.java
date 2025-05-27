package com.academy.dtos.service_provider;

import jakarta.validation.constraints.*;

public record ServiceProviderRequestDTO(
        @NotNull Long memberId,
        @NotNull Long serviceId,
        @Min(0) @Max(3) Integer permission
) {}