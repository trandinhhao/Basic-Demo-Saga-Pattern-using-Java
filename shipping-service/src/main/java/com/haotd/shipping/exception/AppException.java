package com.haotd.shipping.exception;

import com.haotd.shipping.dto.Order;

public class AppException extends RuntimeException {

    public AppException(Order order) {
        this.order = order;
    }

    private Order order;
}
