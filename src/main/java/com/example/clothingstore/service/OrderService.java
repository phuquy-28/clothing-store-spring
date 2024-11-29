package com.example.clothingstore.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.dto.request.OrderPreviewReqDTO;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.OrderReviewReqDTO;
import com.example.clothingstore.dto.request.OrderStatusReqDTO;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.dto.response.OrderPreviewDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.dto.response.OrderReviewDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface OrderService {

  OrderPaymentDTO checkOut(OrderReqDTO orderReqDTO, User user, HttpServletRequest request);

  ResultPaginationDTO getOrdersByUser(Specification<Order> spec, Pageable pageable);

  List<OrderReviewDTO> getOrderReview(Long orderId);

  List<OrderReviewDTO> getLineItemByOrderId(Long orderId);

  OrderReviewReqDTO createOrderReview(OrderReviewReqDTO orderReviewReqDTO);

  OrderReviewReqDTO updateOrderReview(OrderReviewReqDTO orderReviewReqDTO);

  OrderResDTO updateOrderStatus(OrderStatusReqDTO orderStatusReqDTO);

  OrderPreviewDTO previewOrder(OrderPreviewReqDTO orderPreviewReqDTO);
}
