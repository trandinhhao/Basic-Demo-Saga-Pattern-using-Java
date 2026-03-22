package com.haotd.order.camunda;

import com.haotd.order.client.InventoryClient;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("compensateInventoryDelegate")
@RequiredArgsConstructor
@Slf4j
public class CompensateInventoryDelegate implements JavaDelegate {

    private final InventoryClient inventoryClient;
    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        try {
            inventoryClient.cancelInventory(order);
            sagaPersistence.updateStatus(order.getOrderId(), "COMPENSATED_INVENTORY", null);
            log.info("[Saga] Bù hủy kho — đơn {} — thành công", order.getOrderId());
        } catch (Exception e) {
            sagaPersistence.updateStatus(order.getOrderId(), "COMPENSATE_INVENTORY_ERROR", e.getMessage());
            log.warn("[Saga] Bù hủy kho — đơn {} — thất bại: {}", order.getOrderId(), e.getMessage());
        }
    }
}
