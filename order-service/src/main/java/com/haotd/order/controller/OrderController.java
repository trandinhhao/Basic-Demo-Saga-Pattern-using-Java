package com.haotd.order.controller;

import com.haotd.order.dto.OrderRequest;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/process")
    public Order processOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.processOrder(orderRequest);
    }
}
