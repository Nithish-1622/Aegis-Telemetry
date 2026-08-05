package com.aegis.telemetry.config.controller;

import com.aegis.telemetry.config.runtime.RuntimeConfiguration;
import com.aegis.telemetry.config.runtime.RuntimeConfigurationService;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RuntimeConfigurationControllerTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();
    private MockMvc mockMvc;
    private RuntimeConfigurationService runtimeConfigurationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("order-01")
                .environment("production")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());
        runtimeConfigurationService = new RuntimeConfigurationService(sdk);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new RuntimeConfigurationController(runtimeConfigurationService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        sdk.shutdown();
    }

    @Test
    void getsRuntimeConfiguration() throws Exception {
        mockMvc.perform(get("/internal/config/runtime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capturePayload").value(true))
                .andExpect(jsonPath("$.samplingRate").value(100.0));
    }

    @Test
    void updatesRuntimeConfiguration() throws Exception {
        mockMvc.perform(post("/internal/config/runtime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RuntimeConfiguration.builder()
                                .capturePayload(false)
                                .captureThreadInfo(false)
                                .samplingRate(25.0d)
                                .heartbeatIntervalSeconds(10L)
                                .stackTraceEnabled(false)
                                .maximumPayloadSize(2_048)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capturePayload").value(false))
                .andExpect(jsonPath("$.samplingRate").value(25.0));
    }
}
