package com.aegis.demo.inventory;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();

    public Map<String, Object> reserve(Map<String, Object> request, int failTimes) {
        String orderId = String.valueOf(request.getOrDefault("orderId", "default"));
        int currentAttempt = attempts.merge(orderId, 1, Integer::sum);
        if (currentAttempt <= failTimes) {
            throw new IllegalStateException("inventory temporarily unavailable");
        }
        return Map.of("inventoryStatus", "RESERVED", "orderId", orderId);
    }
}
