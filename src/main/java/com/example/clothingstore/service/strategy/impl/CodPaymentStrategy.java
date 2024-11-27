package com.example.clothingstore.service.strategy.impl;

import org.springframework.stereotype.Component;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.service.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CodPaymentStrategy implements PaymentStrategy {
    @Override
    public OrderPaymentDTO processPayment(Order order, HttpServletRequest request) {
        order.setPaymentStatus(PaymentStatus.PENDING);
        
        return OrderPaymentDTO.builder()
            .code(order.getCode())
            .status(order.getStatus())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .paymentUrl(null)
            .build();
    }
}
