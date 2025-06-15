package com.example.clothingstore.service.strategy;

import com.example.clothingstore.entity.Order;
import java.time.Instant;

public interface DeliveryStrategy {

    void processDelivery(Order order);

    double calculateShippingFee(Order order);
    
    double calculateShippingFee(double subtotal);

    Instant calculateEstimatedDeliveryDate(Order order);
}
