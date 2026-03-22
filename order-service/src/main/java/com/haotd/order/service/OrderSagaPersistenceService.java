package com.haotd.order.service;

import com.haotd.order.dto.Order;
import com.haotd.order.entity.OrderSagaEntity;
import com.haotd.order.repository.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderSagaPersistenceService {

    private final OrderSagaRepository repository;

    @Transactional
    public void createPending(Order order) {
        OrderSagaEntity e =
                OrderSagaEntity.builder()
                        .orderId(order.getOrderId())
                        .processInstanceId(null)
                        .productId(order.getProductId())
                        .quantity(order.getQuantity())
                        .status("PENDING_PROCESS")
                        .lastError(null)
                        .build();
        repository.save(e);
    }

    @Transactional
    public void updateProcessInstanceId(String orderId, String processInstanceId) {
        repository
                .findByOrderId(orderId)
                .ifPresent(
                        e -> {
                            e.setProcessInstanceId(processInstanceId);
                            repository.save(e);
                        });
    }

    @Transactional
    public void updateStatus(String orderId, String status, String lastError) {
        repository
                .findByOrderId(orderId)
                .ifPresent(
                        e -> {
                            e.setStatus(status);
                            e.setLastError(lastError);
                            repository.save(e);
                        });
    }

    @Transactional(readOnly = true)
    public Order toOrderDto(String orderId) {
        return repository
                .findByOrderId(orderId)
                .map(
                        e ->
                                Order.builder()
                                        .orderId(e.getOrderId())
                                        .productId(e.getProductId())
                                        .quantity(e.getQuantity())
                                        .status(e.getStatus())
                                        .build())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy order_saga: " + orderId));
    }
}
