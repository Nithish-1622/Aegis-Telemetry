package com.aegis.demo.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/inventory/reserve")
    public ResponseEntity<Map<String, Object>> reserve(@RequestBody Map<String, Object> request,
                                                       @RequestParam(defaultValue = "0") int failTimes) {
        return ResponseEntity.ok(inventoryService.reserve(request, failTimes));
    }
}
