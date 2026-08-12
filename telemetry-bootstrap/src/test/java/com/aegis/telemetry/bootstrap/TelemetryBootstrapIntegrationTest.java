package com.aegis.telemetry.bootstrap;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.contracts.event.RuntimeEvent;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.registry.integration.RegistryIntegrationBridge;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "aegis.runtime.service-name=telemetry-bootstrap",
        "aegis.runtime.instance-id=telemetry-test-01",
        "aegis.runtime.environment=test"
})
class TelemetryBootstrapIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RuntimeTelemetrySdk sdk;

    @Autowired
    private RuntimeEventPublisher publisher;

    @Autowired
    private RegistryIntegrationBridge registryBridge;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().getMessageConverters().stream()
                .filter(org.springframework.http.converter.json.MappingJackson2HttpMessageConverter.class::isInstance)
                .map(org.springframework.http.converter.json.MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.getObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
    }

    @Test
    void platformBeansAreInitialized() {
        assertThat(sdk).isNotNull();
        assertThat(sdk.isInitialized()).isTrue();
        assertThat(sdk.getConfiguration().applicationName()).isEqualTo("telemetry-bootstrap");
        assertThat(publisher).isNotNull();
        assertThat(registryBridge).isNotNull();
    }

    @Test
    void healthEndpointIsUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void demoPingEndpointReturnsPong() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/demo/ping", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("pong");
        assertThat(response.getBody()).containsKey("traceId");
        assertThat(response.getBody()).containsKey("eventId");
    }

    @Test
    void demoTraceEndpointReturnsRequestCompletedEvent() {
        ResponseEntity<RuntimeEvent> response = restTemplate.getForEntity("http://localhost:" + port + "/demo/trace", RuntimeEvent.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().eventType()).isEqualTo(RuntimeEventType.REQUEST_COMPLETED);
        assertThat(response.getBody().serviceName()).isEqualTo("telemetry-bootstrap");
    }

    @Test
    void demoErrorEndpointReturnsInternalServerError() {
        ResponseEntity<RuntimeEvent> response = restTemplate.getForEntity("http://localhost:" + port + "/demo/error", RuntimeEvent.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().eventType()).isEqualTo(RuntimeEventType.ERROR_OCCURRED);
    }
}
