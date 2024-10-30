package com.example.clothingstore.service.strategy.impl;

import org.springframework.stereotype.Component;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.service.VnPayService;
import com.example.clothingstore.service.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VnpayPaymentStrategy implements PaymentStrategy {
    private final VnPayService vnPayService;
    
    @Override
    public OrderPaymentDTO processPayment(Order order, HttpServletRequest request) {
        String paymentUrl = vnPayService.createPaymentUrl(order, request);
        
        return OrderPaymentDTO.builder()
            .code(order.getCode())
            .status(order.getStatus())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .paymentUrl(paymentUrl)
            .build();
    }
}
