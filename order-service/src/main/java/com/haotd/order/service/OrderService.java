package com.haotd.order.service;

import com.haotd.order.client.InventoryClient;
import com.haotd.order.client.PaymentClient;
import com.haotd.order.client.ShippingClient;
import com.haotd.order.dto.OrderRequest;
import com.haotd.order.dto.Order;
import com.haotd.order.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService { // Orchestrator

    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final ShippingClient shippingClient;

    public Order processOrder(OrderRequest orderRequest) {

        Order order = toOrder(orderRequest);
        log.info("[OrderService:processOrder] = {}", order.getStatus());

        try {
            // 1. Call Payment service
            order.setStatus("processPayment");
            log.info("[processOrder] Calling Payment service: {}", order.getStatus());
            paymentClient.processPayment(order);
            log.info("[processOrder] processPayment successfully: {}", order.getStatus());

            // 2. Call Inventory service
            order.setStatus("processInventory");
            log.info("[processOrder] Calling Inventory service: {}", order.getStatus());
            inventoryClient.processInventory(order);
            log.info("[processOrder] processInventory successfully: {}", order.getStatus());

            // 3. Call Shipping service
            order.setStatus("processShipping");
            log.info("[processOrder] Calling Shipping service: {}", order.getStatus());
            shippingClient.processShipping(order);
            log.info("[processOrder] processShipping successfully: {}", order.getStatus());

            // 4. Complete order
            order.setStatus("Completed");
            log.info("[OrderService] Order processed successfully: {}", order.getStatus());
            return order;
        } catch (Exception ex) {
            log.error("[OrderService] Error processing order: {}", ex.getMessage());

            // Compensating transactions
            // 1. Cancel payment
            try {
                log.error("[OrderService] Calling Payment service to cancel payment");
                paymentClient.cancelPayment(order);
            } catch (Exception e) {
                log.error("[OrderService] Error canceling payment: {}", e.getMessage());
            }

            // 2. Cancel inventory
            try {
                log.error("[OrderService] Calling Inventory service to cancel inventory");
                inventoryClient.cancelInventory(order);
            } catch (Exception e) {
                log.error("[OrderService] Error canceling inventory: {}", e.getMessage());
            }

            // 3. Cancel shipping
            try {
                log.error("[OrderService] Calling Shipping service to cancel shipping");
                shippingClient.cancelShipping(order);
            } catch (Exception e) {
                log.error("[OrderService] Error canceling shipping: {}", e.getMessage());
            }

            // 4. Return failure response
            order.setStatus("Failed");
            throw new AppException(order);
        }
    }

    private Order toOrder(OrderRequest orderRequest) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .status("Started")
                .build();
    }
}
