package com.haotd.order.camunda;

import com.haotd.order.client.PaymentClient;
import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("compensatePaymentDelegate")
@RequiredArgsConstructor
@Slf4j
public class CompensatePaymentDelegate implements JavaDelegate {

    private final PaymentClient paymentClient;
    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        try {
            paymentClient.cancelPayment(order);
            sagaPersistence.updateStatus(order.getOrderId(), "COMPENSATED_PAYMENT", null);
            log.info("[Saga] Bù hoàn tiền — đơn {} — thành công", order.getOrderId());
        } catch (Exception e) {
            sagaPersistence.updateStatus(order.getOrderId(), "COMPENSATE_PAYMENT_ERROR", e.getMessage());
            log.warn("[Saga] Bù hoàn tiền — đơn {} — thất bại: {}", order.getOrderId(), e.getMessage());
        }
    }
}
