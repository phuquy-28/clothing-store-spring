package com.example.clothingstore.service.strategy;

import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentStrategy {
    OrderPaymentDTO processPayment(Order order, HttpServletRequest request);
}
