package com.haotd.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationFailedEvent {

    private String orderId;
    private String productId;
    private int quantity;
    private String reason;
}

