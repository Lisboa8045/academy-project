package com.academy.services;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.global_configuration.GlobalConfiguration;
import com.academy.models.global_configuration.GlobalConfigurationType;
import com.academy.repositories.GlobalConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalConfigurationService {

    private GlobalConfigurationRepository configurationRepository;

    @Autowired
    public GlobalConfigurationService(GlobalConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public List<GlobalConfiguration> getAllConfigs() {
        return configurationRepository.findAll();
    }

    public GlobalConfiguration getConfig(String configKey) {
        return configurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new EntityNotFoundException(GlobalConfiguration.class, "No configuration for: " + configKey));
    }

    public void updateConfigValue(String configKey, String configValue, GlobalConfigurationType configType) {
        GlobalConfiguration config = configurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new EntityNotFoundException(GlobalConfiguration.class, "No configuration for: " + configKey));

        config.setConfigValue(configValue);
        config.setConfigType(configType);
        configurationRepository.save(config);
    }




}
