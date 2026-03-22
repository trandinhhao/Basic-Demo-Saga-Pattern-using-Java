package com.haotd.order.camunda;

import com.haotd.order.client.ShippingClient;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processShippingDelegate")
@RequiredArgsConstructor
@Slf4j
public class ProcessShippingDelegate implements JavaDelegate {

    private static final String ERROR_CODE = "SHIPPING_FAILED";

    private final ShippingClient shippingClient;
    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        sagaPersistence.updateStatus(order.getOrderId(), "SHIPPING_PROCESSING", null);
        try {
            Order updated = shippingClient.processShipping(order);
            updated.setStatus("COMPLETED");
            execution.setVariable("order", updated);
            sagaPersistence.updateStatus(updated.getOrderId(), "COMPLETED", null);
            log.info("[Saga] Giao hàng — đơn {} — thành công (saga hoàn tất)", updated.getOrderId());
        } catch (Exception e) {
            sagaPersistence.updateStatus(order.getOrderId(), "SHIPPING_FAILED", e.getMessage());
            log.warn("[Saga] Giao hàng — đơn {} — thất bại: {}", order.getOrderId(), e.getMessage());
            throw new BpmnError(ERROR_CODE, e.getMessage());
        }
    }
}
