package com.aegis.demo.gateway;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContext;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import com.aegis.telemetry.trace.factory.TraceContextFactory;
import com.aegis.telemetry.trace.propagation.TraceHeaderPropagator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DistributedTracePropagationTest {

    private TraceContextFactory gatewayFactory;
    private TraceContextFactory orderFactory;
    private TraceHeaderPropagator propagator;

    @BeforeEach
    void setUp() {
        gatewayFactory = new TraceContextFactory("gateway-service");
        orderFactory = new TraceContextFactory("order-service");
        propagator = new TraceHeaderPropagator();
    }

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
    }

    @Test
    void traceContextIsPropagatedAcrossServiceBoundaries() {
        // 1. Gateway service creates root trace context
        TraceContext rootContext = gatewayFactory.createRootContext();
        TraceContextHolder.setContext(rootContext);

        // 2. Gateway injects headers into outbound HTTP call to Order service
        Map<String, String> outgoingHeaders = new HashMap<>();
        propagator.inject(rootContext, outgoingHeaders);

        assertThat(outgoingHeaders).containsKey(TraceHeaders.TRACE_ID);
        assertThat(outgoingHeaders).containsKey(TraceHeaders.SPAN_ID);
        assertThat(outgoingHeaders.get(TraceHeaders.TRACE_ID)).isEqualTo(rootContext.traceId());
        assertThat(outgoingHeaders.get(TraceHeaders.SPAN_ID)).isEqualTo(rootContext.spanId());

        // 3. Order service extracts trace headers and creates child context
        Optional<TraceContext> extractedContext = propagator.extract(outgoingHeaders);
        assertThat(extractedContext).isPresent();
        assertThat(extractedContext.get().traceId()).isEqualTo(rootContext.traceId());

        TraceContext childContext = orderFactory.createChildContext(extractedContext.get());
        assertThat(childContext.traceId()).isEqualTo(rootContext.traceId());
        assertThat(childContext.parentSpanId()).isEqualTo(rootContext.spanId());
        assertThat(childContext.spanId()).isNotEqualTo(rootContext.spanId());

        // 4. Order service injects context into Payment service call
        Map<String, String> orderToPaymentHeaders = new HashMap<>();
        propagator.inject(childContext, orderToPaymentHeaders);

        assertThat(orderToPaymentHeaders.get(TraceHeaders.TRACE_ID)).isEqualTo(rootContext.traceId());
        assertThat(orderToPaymentHeaders.get(TraceHeaders.SPAN_ID)).isEqualTo(childContext.spanId());
        assertThat(orderToPaymentHeaders.get(TraceHeaders.PARENT_SPAN_ID)).isEqualTo(rootContext.spanId());

        // 5. Payment service extracts downstream context
        Optional<TraceContext> paymentExtracted = propagator.extract(orderToPaymentHeaders);
        assertThat(paymentExtracted).isPresent();
        assertThat(paymentExtracted.get().traceId()).isEqualTo(rootContext.traceId());
        assertThat(paymentExtracted.get().parentSpanId()).isEqualTo(rootContext.spanId());
    }
}
