package com.haotd.order.camunda;

import com.haotd.order.dto.Order;
import com.haotd.order.service.OrderSagaPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("markSagaFailedDelegate")
@RequiredArgsConstructor
@Slf4j
public class MarkSagaFailedDelegate implements JavaDelegate {

    private final OrderSagaPersistenceService sagaPersistence;

    @Override
    public void execute(DelegateExecution execution) {
        Order order = (Order) execution.getVariable("order");
        sagaPersistence.updateStatus(
                order.getOrderId(),
                "SAGA_FAILED",
                "Nhánh bù trừ đã chạy; xem log [Saga] và cột last_error.");
        log.warn("[Saga] Đơn {} — kết thúc thất bại (đã qua bù trừ)", order.getOrderId());
    }
}
