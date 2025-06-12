package com.academy.repositories;

import com.academy.models.global_configuration.GlobalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalConfigurationRepository extends JpaRepository<GlobalConfiguration, Long> {
    Optional<GlobalConfiguration> findByConfigKey(String configKey);
}

