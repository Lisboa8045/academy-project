package com.academy.controllers;

import com.academy.dtos.global_configuration.GlobalConfigurationRequestDTO;
import com.academy.dtos.global_configuration.GlobalConfigurationResponseDTO;
import com.academy.services.GlobalConfigurationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PatchMapping("/{configKey}")
    public ResponseEntity<GlobalConfigurationResponseDTO> updateConfig(@PathVariable String configKey, @Valid @RequestBody GlobalConfigurationRequestDTO request) {
        GlobalConfigurationResponseDTO response = globalConfigurationService.updateConfigValue(configKey, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/edit")
    public ResponseEntity<Void> editConfigs(@Valid @RequestBody List<GlobalConfigurationRequestDTO> request) {
        globalConfigurationService.editConfigs(request);
        return ResponseEntity.ok().build();
    }
}
