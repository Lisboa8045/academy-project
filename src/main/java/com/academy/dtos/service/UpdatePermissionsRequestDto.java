package com.academy.dtos.service;

import com.academy.models.service.service_provider.ProviderPermissionEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePermissionsRequestDto(
        @NotNull List<ProviderPermissionEnum> permissions,
        @NotNull Long memberId) {}
