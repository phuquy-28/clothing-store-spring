package com.example.clothingstore.service.strategy.impl;

import org.springframework.stereotype.Component;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.service.strategy.DeliveryStrategy;

@Component
public class GhnDeliveryStrategy implements DeliveryStrategy {

  @Override
  public void processDelivery(Order order) {
    return;
  }

  @Override
  public double calculateShippingFee(Order order) {
    return 0;
  }
  
}
