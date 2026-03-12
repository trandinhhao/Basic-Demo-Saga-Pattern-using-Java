package com.haotd.inventory.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Builder
public class Product {
    private String productId;
    private int stockQuantity;

    public Product() {
        this.productId = "ticket1";
        this.stockQuantity = 10;
    }
}
