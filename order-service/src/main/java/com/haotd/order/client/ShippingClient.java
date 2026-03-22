package com.haotd.order.client;

import com.haotd.order.dto.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "shipping-service", url = "http://localhost:8082")
public interface ShippingClient {
    @PostMapping(value = "/api/shipping/process", produces = MediaType.APPLICATION_JSON_VALUE)
    Order processShipping(@RequestBody Order order);

    @PostMapping(value = "/api/shipping/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    Order cancelShipping(@RequestBody Order order);
}
