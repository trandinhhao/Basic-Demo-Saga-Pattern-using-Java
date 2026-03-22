package com.haotd.payment.service;

import com.haotd.payment.dto.Order;
import com.haotd.payment.exception.AppException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public Order processPayment(Order order) {
        try {
            Thread.sleep(300);
            order.setStatus("Thanh toán thành công");
            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus("Thanh toán thất bại");
            throw new AppException(order);
        }
    }

    public Order cancelPayment(Order order) {
        try {
            Thread.sleep(300);
            order.setStatus("Đã hoàn tiền");
            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus("Hoàn tiền thất bại");
            throw new AppException(order);
        }
    }
}
