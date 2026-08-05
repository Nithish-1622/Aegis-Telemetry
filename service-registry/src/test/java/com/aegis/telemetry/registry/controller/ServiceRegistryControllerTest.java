package com.aegis.telemetry.registry.controller;

import com.aegis.telemetry.registry.model.ServiceInstance;
import com.aegis.telemetry.registry.service.ServiceRegistryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRegistryControllerTest {

    private MockMvc mockMvc;
    private ServiceRegistryService registryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        registryService = new ServiceRegistryService();
                objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceRegistryController(registryService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registersAndListsServices() throws Exception {
        mockMvc.perform(post("/internal/services/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceRegistrationRequest("payment-service", "1.0.0", "10.0.0.12", 8080, "production"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceId").exists())
                .andExpect(jsonPath("$.status").value("REGISTERED"));

        mockMvc.perform(get("/internal/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("payment-service"));
    }

    @Test
    void deregistersService() throws Exception {
        ServiceInstance instance = registryService.register(ServiceInstance.builder()
                .serviceName("payment-service")
                .instanceId("payment-01")
                .version("1.0.0")
                .host("10.0.0.12")
                .port(8080)
                .environment("production")
                .build());

        mockMvc.perform(delete("/internal/services/{instanceId}", instance.instanceId()))
                .andExpect(status().isNoContent());

        assertThat(registryService.findByInstance(instance.instanceId())).isEmpty();
    }
}
