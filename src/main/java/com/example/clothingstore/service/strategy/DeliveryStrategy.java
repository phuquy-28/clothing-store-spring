package com.example.clothingstore.service.strategy;

import com.example.clothingstore.entity.Order;

public interface DeliveryStrategy {
    void processDelivery(Order order);
    double calculateShippingFee(Order order);
}
