package com.example.clothingstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.annotation.ApiMessage;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.OrderCancelReqDTO;
import com.example.clothingstore.dto.request.OrderPreviewReqDTO;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.OrderReviewReqDTO;
import com.example.clothingstore.dto.request.OrderStatusReqDTO;
import com.example.clothingstore.dto.request.OrderStatisticsSummaryReq;
import com.example.clothingstore.dto.response.OrderDetailsDTO;
import com.example.clothingstore.dto.response.OrderItemList;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.dto.response.OrderPreviewDTO;
import com.example.clothingstore.dto.response.OrderReviewDTO;
import com.example.clothingstore.dto.response.OrderStatisticsSummaryRes;
import com.example.clothingstore.dto.response.MonthlySpendingChartRes;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.StatusSpendingChartRes;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.service.OderCancellationService;
import com.example.clothingstore.service.OrderService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import com.turkraft.springfilter.boot.Filter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class OrderController {

  private final Logger log = LoggerFactory.getLogger(OrderController.class);

  private final OrderService orderService;

  private final UserService userService;

  private final OderCancellationService oderCancellationService;

  @PostMapping(UrlConfig.ORDERS + UrlConfig.PREVIEW)
  public ResponseEntity<OrderPreviewDTO> previewOrder(
      @RequestBody @Valid OrderPreviewReqDTO orderPreviewReqDTO) {
    OrderPreviewDTO result = orderService.previewOrder(orderPreviewReqDTO);
    return ResponseEntity.ok(result);
  }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.CHECK_OUT)
  public ResponseEntity<OrderPaymentDTO> checkOut(@RequestBody @Valid OrderReqDTO orderReqDTO,
      HttpServletRequest request) {
    log.debug("REST request to check out order: {}", orderReqDTO);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (authentication != null && authentication.isAuthenticated())
        ? userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get())
        : null;
    OrderPaymentDTO result = orderService.checkOut(orderReqDTO, user, request);
    return ResponseEntity.ok(result);
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS)
  public ResponseEntity<ResultPaginationDTO> getOrdersByUser(@Filter Specification<Order> spec,
      Pageable pageable) {
    return ResponseEntity.ok(orderService.getOrdersByUser(spec, pageable));
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.ORDER_ID)
  public ResponseEntity<OrderDetailsDTO> getOrderDetailsUser(@PathVariable Long orderId) {
    OrderDetailsDTO result = orderService.getOrderDetailsUser(orderId);
    return ResponseEntity.ok(result);
  }

  // @GetMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.LINE_ITEM +
  // UrlConfig.ORDER_ID)
  // public ResponseEntity<List<OrderReviewDTO>> getLineItemByOrderId(@PathVariable Long orderId) {
  // List<OrderReviewDTO> lineItems = orderService.getLineItemByOrderId(orderId);
  // return ResponseEntity.ok(lineItems);
  // }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.REVIEW)
  public ResponseEntity<OrderReviewReqDTO> createOrderReview(
      @RequestBody @Valid OrderReviewReqDTO orderReviewReqDTO) {
    OrderReviewReqDTO createdOrderReview = orderService.createOrderReview(orderReviewReqDTO);
    return ResponseEntity.ok(createdOrderReview);
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.ORDER_ID + UrlConfig.REVIEW)
  public ResponseEntity<List<OrderReviewDTO>> getOrderReview(@PathVariable Long orderId) {
    List<OrderReviewDTO> orderReviews = orderService.getOrderReview(orderId);
    return ResponseEntity.ok(orderReviews);
  }

  @PutMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.REVIEW)
  public ResponseEntity<OrderReviewReqDTO> updateOrderReview(
      @RequestBody @Valid OrderReviewReqDTO orderReviewReqDTO) {
    OrderReviewReqDTO updatedOrderReview = orderService.updateOrderReview(orderReviewReqDTO);
    return ResponseEntity.ok(updatedOrderReview);
  }

  @PutMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.CANCEL)
  @ApiMessage("Order cancelled successfully")
  public ResponseEntity<Void> cancelOrder(@RequestBody @Valid OrderCancelReqDTO orderCancelReqDTO) {
    log.debug("REST request to cancel order: {}", orderCancelReqDTO);
    oderCancellationService.userCancelOrder(orderCancelReqDTO.getOrderId(),
        orderCancelReqDTO.getReason());
    return ResponseEntity.ok().build();
  }

  @PutMapping(UrlConfig.ORDERS + UrlConfig.STATUS)
  public ResponseEntity<OrderItemList> updateOrderStatus(
      @RequestBody @Valid OrderStatusReqDTO orderStatusReqDTO) {
    OrderItemList updatedOrder = orderService.updateOrderStatus(orderStatusReqDTO);
    return ResponseEntity.ok(updatedOrder);
  }

  @GetMapping(UrlConfig.ORDERS)
  public ResponseEntity<ResultPaginationDTO> getOrderItemList(@Filter Specification<Order> spec,
      Pageable pageable) {
    log.debug("REST request to get order item list: {}", spec);
    return ResponseEntity.ok(orderService.getOrders(spec, pageable));
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.CONTINUE_PAYMENT + UrlConfig.ORDER_ID)
  public ResponseEntity<OrderPaymentDTO> continuePayment(@PathVariable Long orderId) {
    OrderPaymentDTO result = orderService.continuePayment(orderId);
    return ResponseEntity.ok(result);
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.ORDER_ID)
  public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long orderId) {
    OrderDetailsDTO result = orderService.getOrderDetails(orderId);
    return ResponseEntity.ok(result);
  }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.STATISTICS)
  public ResponseEntity<OrderStatisticsSummaryRes> getUserOrderStatistics(
      @RequestBody @Valid OrderStatisticsSummaryReq request) {
    log.debug("REST request to get user order statistics: {}", request);
    OrderStatisticsSummaryRes result = orderService.getUserOrderStatistics(request);
    return ResponseEntity.ok(result);
  }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.STATISTICS + UrlConfig.CHART
      + UrlConfig.LINE)
  public ResponseEntity<MonthlySpendingChartRes> getUserOrderMonthlyChart(
      @RequestBody @Valid OrderStatisticsSummaryReq request) {
    log.debug("REST request to get user order monthly chart data: {}", request);
    MonthlySpendingChartRes result = orderService.getUserOrderMonthlyChart(request);
    return ResponseEntity.ok(result);
  }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.USER_ORDERS + UrlConfig.STATISTICS + UrlConfig.CHART
      + UrlConfig.BAR)
  public ResponseEntity<StatusSpendingChartRes> getUserOrderStatusChart(
      @RequestBody @Valid OrderStatisticsSummaryReq request) {
    log.debug("REST request to get user order status chart data: {}", request);
    StatusSpendingChartRes result = orderService.getUserOrderStatusChart(request);
    return ResponseEntity.ok(result);
  }
}
