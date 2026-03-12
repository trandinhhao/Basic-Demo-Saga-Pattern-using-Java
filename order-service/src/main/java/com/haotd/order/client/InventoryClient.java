package com.haotd.order.client;

import com.haotd.order.dto.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "http://localhost:8081")
public interface InventoryClient {
    @PostMapping(value = "/api/inventory/process", produces = MediaType.APPLICATION_JSON_VALUE)
    public Order processInventory(@RequestBody Order order);

    @PostMapping(value = "/api/inventory/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public Order cancelInventory(@RequestBody Order order);
}
