package com.haotd.shipping.service;

import com.haotd.shipping.dto.Order;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    public Order processShipping(Order order) {
        try {
            Thread.sleep(300);
            order.setStatus("Đã lên lịch giao hàng");
            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus("Giao hàng thất bại");
            throw new IllegalStateException("Lỗi xử lý giao hàng: " + e.getMessage(), e);
        }
    }

    public Order cancelShipping(Order order) {
        try {
            Thread.sleep(300);
            order.setStatus("Đã hủy giao hàng");
            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus("Hủy giao hàng thất bại");
            throw new IllegalStateException("Lỗi hủy giao hàng: " + e.getMessage(), e);
        }
    }
}
