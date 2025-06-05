package com.academy.dtos.global_configuration;

import com.academy.models.global_configuration.GlobalConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class GlobalConfigurationMapper {

    public abstract GlobalConfigurationResponseDTO toDTO(GlobalConfiguration globalConfiguration);
    public abstract GlobalConfiguration toGlobalConfiguration(GlobalConfigurationRequestDTO globalConfigurationRequestDTO);
}
