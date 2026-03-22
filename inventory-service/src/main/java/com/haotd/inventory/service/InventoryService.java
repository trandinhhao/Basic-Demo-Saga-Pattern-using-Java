package com.haotd.inventory.service;

import com.haotd.inventory.dto.Order;
import com.haotd.inventory.exception.AppException;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private static final int TON_KHO_DEMO = 10;

    public Order processInventory(Order order) {
        if (!duTonKho(order.getQuantity())) {
            order.setStatus("Không đủ tồn kho");
            throw new AppException(order);
        }
        try {
            Thread.sleep(300);
            order.setStatus("Đã giữ hàng");
            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus("Giữ hàng thất bại");
            throw new AppException(order);
        }
    }

    public Order cancelInventory(Order order) {
        try {
            Thread.sleep(300);
            order.setStatus("Đã hủy giữ hàng");
            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus("Hủy giữ hàng thất bại");
            throw new AppException(order);
        }
    }

    private boolean duTonKho(int soLuongDat) {
        return TON_KHO_DEMO >= soLuongDat;
    }
}
