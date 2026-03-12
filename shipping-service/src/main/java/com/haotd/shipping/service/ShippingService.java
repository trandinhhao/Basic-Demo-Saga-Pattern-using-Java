package com.haotd.shipping.service;

import com.haotd.shipping.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShippingService {

    public Order processShipping(Order order) {
        log.info("[ShippingService] Processing shipping for order: {}", order);
        // Simulate shipping processing logic
        try {
            Thread.sleep(3000); // Simulate delay
            order.setStatus("Shipping scheduled");
            return order;
        } catch (Exception e) {
            log.error("[ShippingService] Error processing shipping for order: {}", order, e);
            order.setStatus("Shipping failed");
            return order;
        }
    }

    public Order cancelShipping(Order order) {
        log.info("[ShippingService] Canceling shipping for order: {}", order);
        // Simulate shipping cancellation logic
        try {
            Thread.sleep(3000); // Simulate delay
            order.setStatus("Shipping canceled");
            return order;
        } catch (Exception e) {
            log.error("[ShippingService] Error canceling shipping for order: {}", order, e);
            order.setStatus("Shipping cancellation failed");
            return order;
        }
    }
}
