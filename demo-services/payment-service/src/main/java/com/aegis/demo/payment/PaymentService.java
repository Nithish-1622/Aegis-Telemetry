package com.aegis.demo.payment;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {
    public Map<String, Object> process(Map<String, Object> request, long delayMs) {
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        return Map.of("paymentStatus", "APPROVED");
    }
}
