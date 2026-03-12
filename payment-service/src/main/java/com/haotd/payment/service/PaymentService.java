package com.haotd.payment.service;

import com.haotd.payment.dto.Order;
import com.haotd.payment.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    public Order processPayment(Order order) {
        log.info("[PaymentService] Processing payment for order: {}", order.getOrderId());
        // Simulate payment processing logic
        try {
            Thread.sleep(3000); // Simulate delay
            order.setStatus("Payment successful");
            return order;
        } catch (Exception e) {
            log.error("[PaymentService] Error processing payment for order: {}", order.getOrderId(), e);
            order.setStatus("Payment failed");
            throw new AppException(order);
        }
    }

    public Order cancelPayment(Order order) {
        log.info("[PaymentService] Canceling payment for order: {}", order.getOrderId());
        // Simulate payment cancellation logic
        try {
            Thread.sleep(3000); // Simulate delay
            order.setStatus("Payment canceled");
            return order;
        } catch (Exception e) {
            log.error("[PaymentService] Error canceling payment for order: {}", order.getOrderId(), e);
            order.setStatus("Payment cancellation failed");
            throw new AppException(order);
        }
    }
}
