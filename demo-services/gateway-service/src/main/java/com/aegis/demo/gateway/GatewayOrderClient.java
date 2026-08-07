package com.aegis.demo.gateway;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GatewayOrderClient {

    private final RestClient orderRestClient;

    public GatewayOrderClient(RestClient orderRestClient) {
        this.orderRestClient = orderRestClient;
    }

    public Map<String, Object> createOrder(Map<String, Object> request) {
        return orderRestClient.post()
                .uri("/orders")
                .body(request)
                .retrieve()
                .body(Map.class);
    }
}
