package com.haotd.order.service;

import com.haotd.order.dto.Order;
import com.haotd.order.dto.OrderRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String PROCESS_KEY = "OrderSaga";

    private final RuntimeService runtimeService;
    private final OrderSagaPersistenceService sagaPersistence;

    /**
     * Camunda Orchestrator: thanh toán → kho → giao hàng; có bù trừ khi lỗi. Trạng thái lưu bảng {@code order_saga}
     * (Supabase/PostgreSQL).
     */
    public Order processOrder(OrderRequest orderRequest) {
        Order order = toOrder(orderRequest);

        sagaPersistence.createPending(order);

        Map<String, Object> variables = new HashMap<>();
        variables.put("order", order);

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(PROCESS_KEY, order.getOrderId(), variables);

        sagaPersistence.updateProcessInstanceId(order.getOrderId(), processInstance.getId());

        Order result = sagaPersistence.toOrderDto(order.getOrderId());
        log.info("[Saga] Kết thúc — đơn {} — {}", result.getOrderId(), result.getStatus());
        return result;
    }

    private Order toOrder(OrderRequest orderRequest) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .status("STARTING")
                .build();
    }
}
