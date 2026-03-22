package com.haotd.order.client;

import com.haotd.order.dto.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "http://localhost:8080")
public interface PaymentClient {
    @PostMapping(value = "/api/payment/process", produces = MediaType.APPLICATION_JSON_VALUE)
    Order processPayment(@RequestBody Order order);

    @PostMapping(value = "/api/payment/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    Order cancelPayment(@RequestBody Order order);
}
