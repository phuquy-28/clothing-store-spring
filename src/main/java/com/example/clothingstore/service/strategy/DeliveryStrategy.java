package com.example.clothingstore.service.strategy;

import com.example.clothingstore.entity.Order;

public interface DeliveryStrategy {

    void processDelivery(Order order);

    double calculateShippingFee(Order order);
    
    double calculateShippingFee(double subtotal);
    
    double calculateShippingFee(Long districtId, double subtotal);
}
