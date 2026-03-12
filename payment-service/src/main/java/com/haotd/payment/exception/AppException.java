package com.haotd.payment.exception;

import com.haotd.payment.dto.Order;

public class AppException extends RuntimeException {

    public AppException(Order order) {
        this.order = order;
    }

    private Order order;
}
