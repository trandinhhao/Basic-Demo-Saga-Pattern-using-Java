package com.haotd.inventory.service;

import com.haotd.inventory.dto.Order;
import com.haotd.inventory.exception.AppException;
import com.haotd.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Order processInventory(Order order) {
        log.info("Processing inventory for order: {}", order.getOrderId());
        // Simulate inventory processing logic
        if (inventoryRepository.checkAvailable(order)) {
            try {
                Thread.sleep(3000); // Simulate delay
                order.setStatus("Inventory reserved");
                return order;
            } catch (Exception e) {
                log.error("Error processing inventory for order: {}", order.getOrderId(), e);
                order.setStatus("Inventory reservation failed");
                throw new AppException(order);
            }
        } else {
            log.warn("Insufficient inventory for order: {}", order.getOrderId());
            order.setStatus("Insufficient inventory");
            throw new AppException(order);
        }
    }

    public Order cancelInventory(Order order) {
        log.info("Canceling inventory for order: {}", order.getOrderId());
        // Simulate inventory cancellation logic
        try {
            Thread.sleep(3000); // Simulate delay
            order.setStatus("Inventory reservation canceled");
            return order;
        } catch (Exception e) {
            log.error("Error canceling inventory for order: {}", order.getOrderId(), e);
            order.setStatus("Inventory cancellation failed");
            throw new AppException(order);
        }
    }
}

