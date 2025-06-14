package com.example.clothingstore.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.dto.request.OrderPreviewReqDTO;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.OrderReviewReqDTO;
import com.example.clothingstore.dto.request.OrderStatusReqDTO;
import com.example.clothingstore.dto.request.OrderStatisticsSummaryReq;
import com.example.clothingstore.dto.response.MonthlySpendingChartRes;
import com.example.clothingstore.dto.response.OrderDetailsDTO;
import com.example.clothingstore.dto.response.OrderItemList;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.dto.response.OrderPreviewDTO;
import com.example.clothingstore.dto.response.OrderReviewDTO;
import com.example.clothingstore.dto.response.OrderStatisticsSummaryRes;
import com.example.clothingstore.dto.response.OrderStatusHistoryDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.StatusSpendingChartRes;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import com.example.clothingstore.dto.request.CheckQuantityReqDTO;
import com.example.clothingstore.dto.request.MultiMediaUploadReqDTO;
import com.example.clothingstore.dto.response.MultiMediaUploadResDTO;

public interface OrderService {

  OrderPaymentDTO checkOut(OrderReqDTO orderReqDTO, User user, HttpServletRequest request);

  ResultPaginationDTO getOrdersByUser(Specification<Order> spec, Pageable pageable);

  List<OrderReviewDTO> getOrderReview(Long orderId);

  List<OrderReviewDTO> getLineItemByOrderId(Long orderId);

  OrderReviewReqDTO createOrderReview(OrderReviewReqDTO orderReviewReqDTO);

  OrderReviewReqDTO updateOrderReview(OrderReviewReqDTO orderReviewReqDTO);

  OrderItemList updateOrderStatus(OrderStatusReqDTO orderStatusReqDTO);

  OrderPreviewDTO previewOrder(OrderPreviewReqDTO orderPreviewReqDTO);

  ResultPaginationDTO getOrders(Specification<Order> spec, Pageable pageable);

  OrderPaymentDTO continuePayment(Long orderId);

  OrderDetailsDTO getOrderDetailsUser(Long orderId);

  OrderDetailsDTO getOrderDetails(Long orderId);

  OrderStatisticsSummaryRes getUserOrderStatistics(OrderStatisticsSummaryReq request);

  MonthlySpendingChartRes getUserOrderMonthlyChart(OrderStatisticsSummaryReq request);

  StatusSpendingChartRes getUserOrderStatusChart(OrderStatisticsSummaryReq request);

  List<OrderStatusHistoryDTO> getOrderStatusHistory(Long orderId);

  List<OrderStatusHistoryDTO> getOrderStatusHistoryForUser(Long orderId);

  MultiMediaUploadResDTO getReviewMediaUploadUrls(MultiMediaUploadReqDTO uploadRequestDTO);

  OrderDetailsDTO mapToOrderDetailsDTO(Order order);

  void checkQuantity(CheckQuantityReqDTO checkQuantityReqDTO);
}
