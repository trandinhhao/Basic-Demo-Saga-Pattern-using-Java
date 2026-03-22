package com.haotd.order.repository;

import com.haotd.order.entity.OrderSagaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSagaRepository extends JpaRepository<OrderSagaEntity, String> {

    Optional<OrderSagaEntity> findByOrderId(String orderId);
}
