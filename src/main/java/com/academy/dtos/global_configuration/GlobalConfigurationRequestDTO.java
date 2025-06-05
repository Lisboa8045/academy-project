package com.academy.dtos.global_configuration;

import com.academy.models.global_configuration.GlobalConfigurationTypeEnum;
import jakarta.validation.constraints.NotBlank;

public record GlobalConfigurationRequestDTO(
    String configKey,
    @NotBlank String configValue,
    GlobalConfigurationTypeEnum configType) {
}
