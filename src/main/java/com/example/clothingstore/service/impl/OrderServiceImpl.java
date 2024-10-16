package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.ShippingProfileReqDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.entity.LineItem;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.service.OrderService;
import com.example.clothingstore.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import com.example.clothingstore.repository.ProductVariantRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderRepository orderRepository;

  private final VnPayService vnPayService;

  private final ProductVariantRepository productVariantRepository;

  @Override
  public OrderResDTO createCashOrder(OrderReqDTO orderReqDTO, User user) {
    Order order = createOrder(orderReqDTO, user, PaymentMethod.COD);
    OrderResDTO orderResDTO = new OrderResDTO();
    orderResDTO.setCode(order.getCode());
    orderResDTO.setStatus(order.getStatus());
    orderResDTO.setPaymentMethod(order.getPaymentMethod());
    orderResDTO.setPaymentStatus(order.getPaymentStatus());
    orderResDTO.setPaymentUrl(null);
    return orderResDTO;
  }

  @Override
  public OrderResDTO createVnPayOrder(OrderReqDTO orderReqDTO, User user,
      HttpServletRequest request) {
    Order order = createOrder(orderReqDTO, user, PaymentMethod.VNPAY);
    String paymentUrl = vnPayService.createPaymentUrl(order, request);
    OrderResDTO orderResDTO = new OrderResDTO();
    orderResDTO.setCode(order.getCode());
    orderResDTO.setStatus(order.getStatus());
    orderResDTO.setPaymentMethod(order.getPaymentMethod());
    orderResDTO.setPaymentStatus(order.getPaymentStatus());
    orderResDTO.setPaymentUrl(paymentUrl);
    return orderResDTO;
  }

  private Order createOrder(OrderReqDTO orderReqDTO, User user, PaymentMethod paymentMethod) {
    Order order = new Order();
    order.setUser(user);
    order.setOrderDate(Instant.now());
    order.setCode(generateOrderCode());
    order.setNote(orderReqDTO.getNote());
    order.setPaymentMethod(paymentMethod);

    // Set shipping information
    Order.ShippingInformation shippingInfo = new Order.ShippingInformation();
    ShippingProfileReqDTO shippingProfile = orderReqDTO.getShippingProfile();
    shippingInfo.setFirstName(shippingProfile.getFirstName());
    shippingInfo.setLastName(shippingProfile.getLastName());
    shippingInfo.setPhoneNumber(shippingProfile.getPhoneNumber());
    shippingInfo.setAddress(shippingProfile.getAddress());
    shippingInfo.setDistrict(shippingProfile.getDistrict());
    shippingInfo.setProvince(shippingProfile.getProvince());
    shippingInfo.setCountry(shippingProfile.getCountry());
    order.setShippingInformation(shippingInfo);

    // Create and calculate line items
    List<LineItem> lineItems = new ArrayList<>();
    double total = 0.0;
    for (OrderReqDTO.LineItemReqDTO lineItemDTO : orderReqDTO.getLineItems()) {
      LineItem lineItem = new LineItem();
      ProductVariant productVariant =
          productVariantRepository.findById(lineItemDTO.getProductVariantId())
              .orElseThrow(() -> new ResourceNotFoundException("productVariant.not.found"));

      lineItem.setProductVariant(productVariant);
      lineItem.setQuantity(lineItemDTO.getQuantity().longValue());
      lineItem.setUnitPrice(
          productVariant.getProduct().getPrice() + productVariant.getDifferencePrice());
      lineItem.setTotalPrice(lineItem.getUnitPrice() * lineItem.getQuantity());
      lineItem.setOrder(order);

      lineItems.add(lineItem);
      total += lineItem.getTotalPrice();
    }
    order.setLineItems(lineItems);
    order.setTotal(total);

    // Set initial status
    order.setStatus(OrderStatus.PENDING);
    order.setPaymentStatus(PaymentStatus.PENDING);

    // Save the order
    Order savedOrder = orderRepository.save(order);
    log.debug("Saved order with order code: {}", savedOrder.getCode());
    return savedOrder;
  }

  private String generateOrderCode() {
    long timestamp = System.currentTimeMillis();
    return String.format("ORD-%d", timestamp);
  }

}
