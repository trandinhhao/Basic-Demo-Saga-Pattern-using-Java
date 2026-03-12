package com.haotd.shipping.controller;

import com.haotd.shipping.dto.Order;
import com.haotd.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Slf4j
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
