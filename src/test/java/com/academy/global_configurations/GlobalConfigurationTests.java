package com.academy.global_configurations;

import com.academy.dtos.global_configuration.GlobalConfigurationRequestDTO;
import com.academy.dtos.global_configuration.GlobalConfigurationResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.global_configuration.GlobalConfiguration;
import com.academy.models.global_configuration.GlobalConfigurationTypeEnum;
import com.academy.repositories.GlobalConfigurationRepository;
import com.academy.services.GlobalConfigurationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class GlobalConfigurationTests {

    private final GlobalConfigurationService globalConfigurationService;
    private final GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    public GlobalConfigurationTests(GlobalConfigurationService globalConfigurationService,
                                    GlobalConfigurationRepository globalConfigurationRepository) {
        this.globalConfigurationService = globalConfigurationService;
        this.globalConfigurationRepository = globalConfigurationRepository;
    }

    @BeforeEach
    void setUp() {
        this.globalConfigurationRepository.deleteAll();
        GlobalConfiguration config = new GlobalConfiguration();
        config.setConfigKey("configKey_string");
        config.setConfigValue("configValue");
        config.setConfigType(GlobalConfigurationTypeEnum.STRING);
        this.globalConfigurationRepository.save(config);

        config = new GlobalConfiguration();
        config.setConfigKey("configKey_int");
        config.setConfigValue("123");
        config.setConfigType(GlobalConfigurationTypeEnum.INT);
        this.globalConfigurationRepository.save(config);

        config = new GlobalConfiguration();
        config.setConfigKey("configKey_boolean");
        config.setConfigValue("true");
        config.setConfigType(GlobalConfigurationTypeEnum.BOOLEAN);
        this.globalConfigurationRepository.save(config);
    }

    @AfterEach
    void tearDown() {
        this.globalConfigurationRepository.deleteAll();
    }

    @Test
    void testGetAllGlobalConfigurations() {
        List<GlobalConfigurationResponseDTO> configs = this.globalConfigurationService.getAllConfigs();
        GlobalConfigurationResponseDTO config = configs.get(0);
        assertEquals("configKey_string", config.configKey());
        assertEquals("configValue", config.configValue());
        assertEquals(GlobalConfigurationTypeEnum.STRING, config.configType());

        config = configs.get(1);
        assertEquals("configKey_int", config.configKey());
        assertEquals("123", config.configValue());
        assertEquals(GlobalConfigurationTypeEnum.INT, config.configType());

        config = configs.get(2);
        assertEquals("configKey_boolean", config.configKey());
        assertEquals("true", config.configValue());
        assertEquals(GlobalConfigurationTypeEnum.BOOLEAN, config.configType());
    }

    @Test
    void testGetConfigByConfigKey() {
        GlobalConfigurationResponseDTO config = this.globalConfigurationService.getConfig("configKey_string");
        assertEquals("configKey_string", config.configKey());
        assertEquals("configValue", config.configValue());
        assertEquals(GlobalConfigurationTypeEnum.STRING, config.configType());
    }

    @Test
    void testGetConfigByConfigKey_notFound() {
        assertThrows(EntityNotFoundException.class,
                () -> this.globalConfigurationService.getConfig("Non_existent_config_key"));
    }

    @Test
    void testUpdateGlobalConfiguration() {
        GlobalConfigurationRequestDTO request = new GlobalConfigurationRequestDTO(null, "454", null);
        GlobalConfigurationResponseDTO config = globalConfigurationService.updateConfigValue("configKey_int", request);
        assertEquals("454", config.configValue());
    }

    @Test
    void testUpdateGlobalConfiguration_notFound() {
        GlobalConfigurationRequestDTO request = new GlobalConfigurationRequestDTO(null, "454", null);
        assertThrows(EntityNotFoundException.class,
        () -> globalConfigurationService.updateConfigValue("Non_existent_config_key", request));
    }

    @Test
    void testUpdateGlobalConfiguration_wrongType() {
        GlobalConfigurationRequestDTO request = new GlobalConfigurationRequestDTO(null, "grega", null);
        assertThrows(InvalidArgumentException.class,
                () -> globalConfigurationService.updateConfigValue("configKey_boolean", request));
    }
}
