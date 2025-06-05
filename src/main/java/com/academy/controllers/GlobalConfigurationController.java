package com.academy.controllers;

import com.academy.dtos.global_configuration.GlobalConfigurationRequestDTO;
import com.academy.dtos.global_configuration.GlobalConfigurationResponseDTO;
import com.academy.services.GlobalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/global_configurations")
public class GlobalConfigurationController {
    private final GlobalConfigurationService globalConfigurationService;

    @Autowired
    public GlobalConfigurationController(GlobalConfigurationService globalConfigurationService) {
        this.globalConfigurationService = globalConfigurationService;
    }

    @GetMapping
    public ResponseEntity<List<GlobalConfigurationResponseDTO>> getAll(){
        List<GlobalConfigurationResponseDTO> responses = globalConfigurationService.getAllConfigs();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{configKey}")
    public ResponseEntity<GlobalConfigurationResponseDTO> getConfig(@PathVariable String configKey){
        GlobalConfigurationResponseDTO response = globalConfigurationService.getConfig(configKey);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalConfigurationResponseDTO> updateConfig(@PathVariable long id, GlobalConfigurationRequestDTO request) {
        GlobalConfigurationResponseDTO response = globalConfigurationService.updateConfigValue(id, request);
        return ResponseEntity.ok(response);
    }
}
