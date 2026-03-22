package com.haotd.shipping.controller;

import com.haotd.shipping.dto.Order;
import com.haotd.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/process")
    public Order processShipping(@RequestBody Order order) {
        return shippingService.processShipping(order);
    }

    @PostMapping("/cancel")
    public Order cancelShipping(@RequestBody Order order) {
        return shippingService.cancelShipping(order);
    }
}
