package com.haotd.order.camunda;

import com.haotd.order.client.PaymentClient;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processPaymentDelegate")
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentDelegate implements JavaDelegate {

    private static final String ERROR_CODE = "PAYMENT_FAILED";

    private final PaymentClient paymentClient;
    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        sagaPersistence.updateStatus(order.getOrderId(), "PAYMENT_PROCESSING", null);
        try {
            Order updated = paymentClient.processPayment(order);
            updated.setStatus("PAID");
            execution.setVariable("order", updated);
            sagaPersistence.updateStatus(updated.getOrderId(), "PAID", null);
            log.info("[Saga] Thanh toán — đơn {} — thành công", updated.getOrderId());
        } catch (Exception e) {
            sagaPersistence.updateStatus(order.getOrderId(), "PAYMENT_FAILED", e.getMessage());
            log.warn("[Saga] Thanh toán — đơn {} — thất bại: {}", order.getOrderId(), e.getMessage());
            throw new BpmnError(ERROR_CODE, e.getMessage());
        }
    }
}
