package com.example.clothingstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.OrderPreviewReqDTO;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.OrderReviewReqDTO;
import com.example.clothingstore.dto.request.OrderStatusReqDTO;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.dto.response.OrderPreviewDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.dto.response.OrderReviewDTO;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.service.OrderService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class OrderController {

  private final Logger log = LoggerFactory.getLogger(OrderController.class);

  private final OrderService orderService;

  private final UserService userService;

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

  @GetMapping(UrlConfig.ORDERS + UrlConfig.MY_ORDERS)
  public ResponseEntity<List<OrderResDTO>> getOrdersByUser() {
    List<OrderResDTO> orders = orderService.getOrdersByUser();
    return ResponseEntity.ok(orders);
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.MY_ORDERS + UrlConfig.LINE_ITEM + UrlConfig.ORDER_ID)
  public ResponseEntity<List<OrderReviewDTO>> getLineItemByOrderId(@PathVariable Long orderId) {
    List<OrderReviewDTO> lineItems = orderService.getLineItemByOrderId(orderId);
    return ResponseEntity.ok(lineItems);
  }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.MY_ORDERS + UrlConfig.REVIEW)
  public ResponseEntity<OrderReviewReqDTO> createOrderReview(
      @RequestBody @Valid OrderReviewReqDTO orderReviewReqDTO) {
    OrderReviewReqDTO createdOrderReview = orderService.createOrderReview(orderReviewReqDTO);
    return ResponseEntity.ok(createdOrderReview);
  }

  @GetMapping(UrlConfig.ORDERS + UrlConfig.MY_ORDERS + UrlConfig.REVIEW + UrlConfig.ORDER_ID)
  public ResponseEntity<List<OrderReviewDTO>> getOrderReview(@PathVariable Long orderId) {
    List<OrderReviewDTO> orderReviews = orderService.getOrderReview(orderId);
    return ResponseEntity.ok(orderReviews);
  }

  @PutMapping(UrlConfig.ORDERS + UrlConfig.MY_ORDERS + UrlConfig.REVIEW)
  public ResponseEntity<OrderReviewReqDTO> updateOrderReview(
      @RequestBody @Valid OrderReviewReqDTO orderReviewReqDTO) {
    OrderReviewReqDTO updatedOrderReview = orderService.updateOrderReview(orderReviewReqDTO);
    return ResponseEntity.ok(updatedOrderReview);
  }

  @PutMapping(UrlConfig.ORDERS + UrlConfig.MY_ORDERS + UrlConfig.STATUS)
  public ResponseEntity<OrderResDTO> updateOrderStatus(
      @RequestBody @Valid OrderStatusReqDTO orderStatusReqDTO) {
    OrderResDTO updatedOrder = orderService.updateOrderStatus(orderStatusReqDTO);
    return ResponseEntity.ok(updatedOrder);
  }
}
