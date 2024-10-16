package com.example.clothingstore.service;

import com.example.clothingstore.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface VnPayService {
  
  String createPaymentUrl(Order order, HttpServletRequest request);

  Void validatePayment(HttpServletRequest request);
}
