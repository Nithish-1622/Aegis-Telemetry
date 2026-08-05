package com.aegis.telemetry.config.controller;

import com.aegis.telemetry.config.runtime.RuntimeConfiguration;
import com.aegis.telemetry.config.runtime.RuntimeConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/config/runtime")
public class RuntimeConfigurationController {

    private final RuntimeConfigurationService runtimeConfigurationService;

    public RuntimeConfigurationController(RuntimeConfigurationService runtimeConfigurationService) {
        this.runtimeConfigurationService = runtimeConfigurationService;
    }

    @GetMapping
    public RuntimeConfiguration getConfiguration() {
        return runtimeConfigurationService.getConfiguration();
    }

    @PostMapping
    public ResponseEntity<RuntimeConfiguration> updateConfiguration(@Valid @RequestBody RuntimeConfiguration request) {
        return ResponseEntity.ok(runtimeConfigurationService.updateConfiguration(request));
    }
}
