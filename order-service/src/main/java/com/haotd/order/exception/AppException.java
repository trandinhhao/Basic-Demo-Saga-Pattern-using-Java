package com.haotd.order.exception;

import com.haotd.order.dto.Order;

public class AppException extends RuntimeException {

    public AppException(Order orderResponse) {
        this.orderResponse = orderResponse;
    }

    private Order orderResponse;
}
