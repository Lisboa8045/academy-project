package com.academy.services;

import com.academy.dtos.global_configuration.GlobalConfigurationMapper;
import com.academy.dtos.global_configuration.GlobalConfigurationRequestDTO;
import com.academy.dtos.global_configuration.GlobalConfigurationResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.global_configuration.GlobalConfiguration;
import com.academy.models.global_configuration.GlobalConfigurationTypeEnum;
import com.academy.repositories.GlobalConfigurationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalConfigurationService {

    private final GlobalConfigurationRepository configurationRepository;
    private final GlobalConfigurationMapper globalConfigurationMapper;

    @Autowired
    public GlobalConfigurationService(GlobalConfigurationRepository configurationRepository, GlobalConfigurationMapper globalConfigurationMapper) {
        this.configurationRepository = configurationRepository;
        this.globalConfigurationMapper = globalConfigurationMapper;
    }

    public List<GlobalConfigurationResponseDTO> getAllConfigs() {
        return configurationRepository.findAll()
                .stream().map(globalConfigurationMapper::toDTO)
                .toList();
    }

    public GlobalConfigurationResponseDTO getConfig(String configKey) {
        GlobalConfiguration config = configurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new EntityNotFoundException(GlobalConfiguration.class, "No configuration for: " + configKey));
        return globalConfigurationMapper.toDTO(config);
    }

    @Transactional
    public GlobalConfigurationResponseDTO updateConfigValue(String configKey, GlobalConfigurationRequestDTO request) {
        GlobalConfiguration config = configurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new EntityNotFoundException(GlobalConfiguration.class, "No configuration for: " + configKey));

        String newValue = request.configValue();
        if(isNotAValidValue(newValue, config.getConfigType())){
            throw new InvalidArgumentException(newValue + " is not a valid value for type " + config.getConfigType());
        }

        config.setConfigValue(request.configValue());
        return globalConfigurationMapper.toDTO(configurationRepository.save(config));
    }

    private boolean isNotAValidValue(String value, GlobalConfigurationTypeEnum type) {
        try {
            switch (type) {
                case INT:
                    Integer.parseInt(value);
                    break;
                case BOOLEAN:
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        return true;
                    }
                    break;
                case STRING:
                    // All values are valid strings
                    break;
                default:
                    return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }

    }




}
