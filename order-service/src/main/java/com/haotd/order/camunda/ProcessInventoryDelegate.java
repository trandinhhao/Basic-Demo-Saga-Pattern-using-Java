package com.haotd.order.camunda;

import com.haotd.order.client.InventoryClient;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processInventoryDelegate")
@RequiredArgsConstructor
@Slf4j
public class ProcessInventoryDelegate implements JavaDelegate {

    private static final String ERROR_CODE = "INVENTORY_FAILED";

    private final InventoryClient inventoryClient;
    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        sagaPersistence.updateStatus(order.getOrderId(), "INVENTORY_PROCESSING", null);
        try {
            Order updated = inventoryClient.processInventory(order);
            updated.setStatus("INVENTORY_RESERVED");
            execution.setVariable("order", updated);
            sagaPersistence.updateStatus(updated.getOrderId(), "INVENTORY_RESERVED", null);
            log.info("[Saga] Kho — đơn {} — thành công", updated.getOrderId());
        } catch (Exception e) {
            sagaPersistence.updateStatus(order.getOrderId(), "INVENTORY_FAILED", e.getMessage());
            log.warn("[Saga] Kho — đơn {} — thất bại: {}", order.getOrderId(), e.getMessage());
            throw new BpmnError(ERROR_CODE, e.getMessage());
        }
    }
}
