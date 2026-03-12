package com.haotd.inventory.exception;

import com.haotd.inventory.dto.Order;

public class AppException extends RuntimeException {

    public AppException(Order order) {
        this.order = order;
    }

    private Order order;
}
