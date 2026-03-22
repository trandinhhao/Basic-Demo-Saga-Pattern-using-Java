package com.haotd.inventory.controller;

import com.haotd.inventory.dto.Order;
import com.haotd.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/process")
    public Order processInventory(@RequestBody Order order) {
        return inventoryService.processInventory(order);
    }

    @PostMapping("/cancel")
    public Order cancelInventory(@RequestBody Order order) {
        return inventoryService.cancelInventory(order);
    }
}
