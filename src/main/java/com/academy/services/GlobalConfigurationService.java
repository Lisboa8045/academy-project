package com.academy.services;

import com.academy.dtos.global_configuration.GlobalConfigurationMapper;
import com.academy.dtos.global_configuration.GlobalConfigurationRequestDTO;
import com.academy.dtos.global_configuration.GlobalConfigurationResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.global_configuration.GlobalConfiguration;
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
    public GlobalConfigurationResponseDTO updateConfigValue(long id, GlobalConfigurationRequestDTO request) {
        GlobalConfiguration oldConfig = configurationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(GlobalConfiguration.class, id));

        GlobalConfiguration newConfig = globalConfigurationMapper.toGlobalConfiguration(request);
        newConfig.setId(oldConfig.getId());
        return globalConfigurationMapper.toDTO(configurationRepository.save(newConfig));
    }




}
