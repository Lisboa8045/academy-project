package com.academy.dtos.service_provider;

import com.academy.models.service.service_provider.ProviderPermissionEnum;
import jakarta.validation.constraints.*;

import java.util.List;

public record ServiceProviderRequestDTO(
        @NotNull Long memberId,
        @NotNull Long serviceId,
        @NotNull List<ProviderPermissionEnum> permissions
) {}