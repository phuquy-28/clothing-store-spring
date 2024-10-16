package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface OrderService {

  OrderResDTO createCashOrder(OrderReqDTO orderReqDTO, User user);

  OrderResDTO createVnPayOrder(OrderReqDTO orderReqDTO, User user, HttpServletRequest request);
}
