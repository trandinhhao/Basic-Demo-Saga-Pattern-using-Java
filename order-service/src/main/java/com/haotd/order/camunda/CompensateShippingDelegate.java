package com.haotd.order.camunda;

import com.haotd.order.client.ShippingClient;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("compensateShippingDelegate")
@RequiredArgsConstructor
@Slf4j
public class CompensateShippingDelegate implements JavaDelegate {

    private final ShippingClient shippingClient;
    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        try {
            shippingClient.cancelShipping(order);
            sagaPersistence.updateStatus(order.getOrderId(), "COMPENSATED_SHIPPING", null);
            log.info("[Saga] Bù hủy giao hàng — đơn {} — thành công", order.getOrderId());
        } catch (Exception e) {
            sagaPersistence.updateStatus(order.getOrderId(), "COMPENSATE_SHIPPING_ERROR", e.getMessage());
            log.warn("[Saga] Bù hủy giao hàng — đơn {} — thất bại: {}", order.getOrderId(), e.getMessage());
        }
    }
}
