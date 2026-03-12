package com.haotd.inventory.repository;

import com.haotd.inventory.dto.Order;
import com.haotd.inventory.entity.Product;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {

     public boolean checkAvailable(Order order) {
         Product p = new Product();

         if (p.getStockQuantity() > order.getQuantity()) {
             return true;
         } else {
             return false;
         }
     }
}
