package com.aegis.telemetry.registry.heartbeat;

import com.aegis.telemetry.registry.config.ServiceRegistryProperties;
import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.model.ServiceStatus;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HeartbeatControllerTest {

    private MockMvc mockMvc;
    private ServiceRegistryService registryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        registryService = new ServiceRegistryService();
        ServiceRegistryProperties properties = new ServiceRegistryProperties();
        properties.setHeartbeatTimeoutMs(1_000L);
        HeartbeatService heartbeatService = new HeartbeatService(registryService, properties);
        mockMvc = MockMvcBuilders.standaloneSetup(new HeartbeatController(heartbeatService))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules()))
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
        registryService.register(ServiceInstance.builder()
                .serviceName("payment-service")
                .instanceId("payment-01")
                .version("1.0.0")
                .host("10.0.0.12")
                .port(8080)
                .environment("production")
                .status(ServiceStatus.REGISTERED)
                .registeredAt(Instant.now())
                .build());
    }

    @Test
    void acceptsHeartbeat() throws Exception {
        mockMvc.perform(post("/internal/services/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HeartbeatRequest("payment-service", "payment-01", Instant.parse("2026-07-21T00:01:00Z")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instanceId").value("payment-01"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
