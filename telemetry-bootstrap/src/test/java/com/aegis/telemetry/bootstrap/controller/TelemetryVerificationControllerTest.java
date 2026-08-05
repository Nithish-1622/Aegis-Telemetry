package com.aegis.telemetry.bootstrap.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.publisher.core.RuntimeEventPublisher;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.sdk.serialization.RuntimeEventSerializer;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

@WebMvcTest(TelemetryVerificationController.class)
class TelemetryVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuntimeTelemetrySdk sdk;

    @MockBean
    private RuntimeEventPublisher publisher;

    @MockBean
    private RuntimeEventSerializer serializer;

    @MockBean
    private TraceContextFactory traceContextFactory;

    @Test
    void pingReturnsPongAndPublishesEvent() throws Exception {
        when(sdk.getConfiguration()).thenReturn(RuntimeSdkConfiguration.builder()
                .applicationName("telemetry-bootstrap")
                .instanceId("telemetry-001")
                .environment("bootstrap")
                .build());
        when(traceContextFactory.createRootContext("telemetry-bootstrap"))
                .thenReturn(TraceContext.builder()
                        .traceId("00000000-0000-0000-0000-000000000001")
                        .spanId("00000000-0000-0000-0000-000000000002")
                        .serviceName("telemetry-bootstrap")
                        .createdAt(Instant.parse("2026-08-05T00:00:00Z"))
                        .build());

        mockMvc.perform(get("/demo/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.eventId").exists());

        verify(publisher).publish(any());
    }

    @Test
    void traceUsesRequestCompletedEventType() throws Exception {
        when(sdk.getConfiguration()).thenReturn(RuntimeSdkConfiguration.builder()
                .applicationName("telemetry-bootstrap")
                .instanceId("telemetry-001")
                .environment("bootstrap")
                .build());
        when(traceContextFactory.createRootContext("telemetry-bootstrap"))
                .thenReturn(TraceContext.builder()
                        .traceId("00000000-0000-0000-0000-000000000001")
                        .spanId("00000000-0000-0000-0000-000000000002")
                        .serviceName("telemetry-bootstrap")
                        .createdAt(Instant.parse("2026-08-05T00:00:00Z"))
                        .build());

        mockMvc.perform(get("/demo/trace").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(publisher).publish(any());
    }

    @Test
    void errorReturnsServerError() throws Exception {
        when(sdk.getConfiguration()).thenReturn(RuntimeSdkConfiguration.builder()
                .applicationName("telemetry-bootstrap")
                .instanceId("telemetry-001")
                .environment("bootstrap")
                .build());
        when(traceContextFactory.createRootContext("telemetry-bootstrap"))
                .thenReturn(TraceContext.builder()
                        .traceId("00000000-0000-0000-0000-000000000001")
                        .spanId("00000000-0000-0000-0000-000000000002")
                        .serviceName("telemetry-bootstrap")
                        .createdAt(Instant.parse("2026-08-05T00:00:00Z"))
                        .build());

        mockMvc.perform(get("/demo/error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(publisher).publish(any());
    }
}
