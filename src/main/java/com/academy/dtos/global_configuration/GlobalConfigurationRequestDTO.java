package com.academy.dtos.global_configuration;

import com.academy.models.global_configuration.GlobalConfigurationTypeEnum;

public record GlobalConfigurationRequestDTO(
    Long id,
    String configKey,
    String configValue,
    GlobalConfigurationTypeEnum configType) {
}
