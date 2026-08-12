package com.aegis.demo.order;

import com.aegis.telemetry.contracts.trace.TraceHeaders;
import com.aegis.telemetry.trace.context.TraceContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final RestClient paymentClient;
    private final RestClient inventoryClient;
    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();

    public OrderService(
            RestClient.Builder builder,
            @Value("${app.payment.base-url:http://localhost:8082}") String paymentBaseUrl,
            @Value("${app.inventory.base-url:http://localhost:8083}") String inventoryBaseUrl) {
        this.paymentClient = withTrace(builder.baseUrl(paymentBaseUrl));
        this.inventoryClient = withTrace(builder.baseUrl(inventoryBaseUrl));
    }

    public Map<String, Object> create(Map<String, Object> request) {
        String orderId = UUID.randomUUID().toString();
        Map<String, Object> payload = new HashMap<>(request);
        payload.put("orderId", orderId);
        paymentClient.post().uri("/payments/process").body(payload).retrieve().toBodilessEntity();
        inventoryClient.post().uri("/inventory/reserve").body(payload).retrieve().toBodilessEntity();
        Map<String, Object> response = Map.of("orderId", orderId, "status", "CREATED");
        orders.put(orderId, response);
        return response;
    }

    public Map<String, Object> get(String orderId) {
        return orders.getOrDefault(orderId, Map.of("orderId", orderId, "status", "NOT_FOUND"));
    }

    private RestClient withTrace(RestClient.Builder builder) {
        return builder.requestInterceptor((request, body, execution) -> {
            TraceContextHolder.getContext().ifPresent(context -> {
                request.getHeaders().set(TraceHeaders.TRACE_ID, context.traceId());
                request.getHeaders().set(TraceHeaders.SPAN_ID, context.spanId());
                if (context.parentSpanId() != null) {
                    request.getHeaders().set(TraceHeaders.PARENT_SPAN_ID, context.parentSpanId());
                }
            });
            return execution.execute(request, body);
        }).build();
    }
}
