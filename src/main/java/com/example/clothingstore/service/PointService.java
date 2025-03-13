package com.example.clothingstore.service;

import com.example.clothingstore.entity.Order;

public interface PointService {

  void addPointsFromOrder(Order order);

  void refundPointsFromOrder(Order order);

  Long calculatePointsFromAmount(Double amount);

  Double calculateAmountFromPoints(Long points);
}
