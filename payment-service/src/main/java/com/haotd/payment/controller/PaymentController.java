package com.haotd.payment.controller;

import com.haotd.payment.dto.Order;
import com.haotd.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public Order processPayment(@RequestBody Order response) {
        return paymentService.processPayment(response);
    }

    @PostMapping("/cancel")
    public Order cancelPayment(@RequestBody Order response) {
        return paymentService.cancelPayment(response);
    }
}
