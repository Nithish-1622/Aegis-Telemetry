package com.aegis.demo.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GatewayController {

    private final GatewayOrderClient gatewayOrderClient;

    public GatewayController(GatewayOrderClient gatewayOrderClient) {
        this.gatewayOrderClient = gatewayOrderClient;
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(gatewayOrderClient.createOrder(request));
    }
}
