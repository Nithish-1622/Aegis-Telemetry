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

    public OrderService(@Value("${app.payment.base-url:http://localhost:8082}") String paymentBaseUrl,
                        @Value("${app.inventory.base-url:http://localhost:8083}") String inventoryBaseUrl,
                        RestClient.Builder builder) {
        this.paymentClient = builder.baseUrl(paymentBaseUrl).requestInterceptor((request, body, execution) -> {
            TraceContextHolder.getContext().ifPresent(context -> {
                request.getHeaders().set(TraceHeaders.TRACE_ID, context.traceId());
                request.getHeaders().set(TraceHeaders.SPAN_ID, context.spanId());
                if (context.parentSpanId() != null) {
                    request.getHeaders().set(TraceHeaders.PARENT_SPAN_ID, context.parentSpanId());
                }
            });
            return execution.execute(request, body);
        }).build();
        this.inventoryClient = builder.baseUrl(inventoryBaseUrl).requestInterceptor((request, body, execution) -> {
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

    public Map<String, Object> create(Map<String, Object> request) {
        String id = UUID.randomUUID().toString();
        paymentClient.post().uri("/payments/process").body(request).retrieve().toBodilessEntity();
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                inventoryClient.post().uri("/inventory/reserve").body(request).retrieve().toBodilessEntity();
                break;
            } catch (Exception ex) {
                if (attempts >= 3) throw ex;
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", id);
        response.put("status", "CREATED");
        orders.put(id, response);
        return response;
    }

    public Map<String, Object> get(String id) {
        return orders.getOrDefault(id, Map.of("orderId", id, "status", "NOT_FOUND"));
    }
}
