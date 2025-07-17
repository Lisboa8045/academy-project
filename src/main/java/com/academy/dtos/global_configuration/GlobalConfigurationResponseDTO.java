package com.academy.dtos.global_configuration;

import com.academy.models.global_configuration.GlobalConfigurationTypeEnum;

public record GlobalConfigurationResponseDTO(
        Long id,
        String configName,
        String configKey,
        String configValue,
        GlobalConfigurationTypeEnum configType) {
}
