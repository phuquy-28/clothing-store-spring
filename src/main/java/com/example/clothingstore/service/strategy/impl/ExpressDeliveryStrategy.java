package com.example.clothingstore.service.strategy.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.service.strategy.DeliveryStrategy;

@Component
public class ExpressDeliveryStrategy implements DeliveryStrategy {

  @Value("${ghn.shop.free-shipping-threshold}")
  private Long freeShippingThreshold;

  @Override
  public void processDelivery(Order order) {
    return;
  }

  @Override
  public double calculateShippingFee(Order order) {
    return 0;
  }

  @Override
  public double calculateShippingFee(double subtotal) {
    if (subtotal < freeShippingThreshold) {
      return 50000;
    }
    return 0;
  }

  @Override
  public double calculateShippingFee(Long districtId, double subtotal) {
    if (subtotal < freeShippingThreshold) {
      return 50000;
    }
    return 0;
  }
}
