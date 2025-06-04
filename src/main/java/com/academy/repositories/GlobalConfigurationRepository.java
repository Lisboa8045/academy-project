package com.academy.repositories;

import com.academy.models.global_configuration.GlobalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalConfigurationRepository extends JpaRepository<GlobalConfiguration, Long> {
    GlobalConfiguration findByConfigKey(String configKey);
}

