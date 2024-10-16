package com.example.clothingstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
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

  @PostMapping(UrlConfig.ORDERS + UrlConfig.PAY_CASH)
  public ResponseEntity<OrderResDTO> createOrder(@RequestBody @Valid OrderReqDTO orderReqDTO) {
    log.debug("REST request to create order: {}", orderReqDTO);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (authentication != null && authentication.isAuthenticated())
        ? userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get())
        : null;
    OrderResDTO createdOrder = orderService.createCashOrder(orderReqDTO, user);
    return ResponseEntity.ok(createdOrder);
  }

  @PostMapping(UrlConfig.ORDERS + UrlConfig.PAY_VNPAY)
  public ResponseEntity<OrderResDTO> createOrderVNPay(@RequestBody @Valid OrderReqDTO orderReqDTO,
      HttpServletRequest request) {
    log.debug("REST request to create order: {}", orderReqDTO);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (authentication != null && authentication.isAuthenticated())
        ? userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get())
        : null;
    OrderResDTO createdOrder = orderService.createVnPayOrder(orderReqDTO, user, request);
    return ResponseEntity.ok(createdOrder);
  }

}
