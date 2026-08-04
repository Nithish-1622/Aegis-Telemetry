package com.aegis.telemetry.instrumentation.web;

import com.aegis.telemetry.contracts.enums.RuntimeEventType;
import com.aegis.telemetry.instrumentation.exception.RuntimeExceptionHandler;
import com.aegis.telemetry.instrumentation.integration.RuntimeInstrumentationBridge;
import com.aegis.telemetry.instrumentation.metadata.RuntimeMetadataProvider;
import com.aegis.telemetry.instrumentation.thread.ThreadMetadataProvider;
import com.aegis.telemetry.sdk.RuntimeTelemetrySdk;
import com.aegis.telemetry.sdk.config.RuntimeSdkConfiguration;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeRequestInterceptorTest {

    private final RuntimeTelemetrySdk sdk = RuntimeTelemetrySdk.getInstance();

    @AfterEach
    void tearDown() {
        sdk.shutdown();
        TraceContextHolder.clear();
    }

    @Test
    void createsRequestEventsPropagatesTraceAndClearsContext() throws Exception {
        sdk.initialize(RuntimeSdkConfiguration.builder()
                .applicationName("order-service")
                .instanceId("instance-1")
                .environment("test")
                .heartbeatInterval(Duration.ofSeconds(30))
                .build());

        RuntimeInstrumentationBridge bridge = new RuntimeInstrumentationBridge(sdk, new RuntimeMetadataProvider(sdk), new ThreadMetadataProvider());
        RuntimeRequestInterceptor interceptor = new RuntimeRequestInterceptor(sdk, new TraceContextFactory("order-service"), bridge, new RuntimeExceptionHandler(bridge));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/1");
        request.addHeader("X-Trace-Id", "11111111-1111-1111-1111-111111111111");
        request.addHeader("X-Span-Id", "22222222-2222-2222-2222-222222222222");
        request.addHeader("X-Parent-Span-Id", "33333333-3333-3333-3333-333333333333");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(request.getAttribute(RuntimeRequestInterceptor.REQUEST_STARTED_EVENT_ATTRIBUTE)).satisfies(attribute -> {
            assertThat(attribute).isNotNull();
            assertThat(((com.aegis.telemetry.contracts.event.RuntimeEvent) attribute).eventType()).isEqualTo(RuntimeEventType.REQUEST_STARTED);
            assertThat(((com.aegis.telemetry.contracts.event.RuntimeEvent) attribute).traceId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        });
        assertThat(TraceContextHolder.getContext()).isPresent();

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(request.getAttribute(RuntimeRequestInterceptor.REQUEST_COMPLETED_EVENT_ATTRIBUTE)).isNotNull();
        assertThat(TraceContextHolder.getContext()).isEmpty();
        assertThat(sdk.getCurrentTraceContext()).isEmpty();
    }
}
