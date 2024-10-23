package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.OrderReviewReqDTO;
import com.example.clothingstore.dto.request.OrderStatusReqDTO;
import com.example.clothingstore.dto.request.ShippingProfileReqDTO;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.dto.response.OrderReviewDTO;
import com.example.clothingstore.entity.LineItem;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.Review;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceAlreadyExistException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.service.OrderService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.service.VnPayService;
import com.example.clothingstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.repository.ReviewRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderRepository orderRepository;

  private final VnPayService vnPayService;

  private final ProductVariantRepository productVariantRepository;

  private final UserService userService;

  private final ReviewRepository reviewRepository;

  @Override
  public OrderPaymentDTO createCashOrder(OrderReqDTO orderReqDTO, User user) {
    Order order = createOrder(orderReqDTO, user, PaymentMethod.COD);
    OrderPaymentDTO orderResDTO = new OrderPaymentDTO();
    orderResDTO.setCode(order.getCode());
    orderResDTO.setStatus(order.getStatus());
    orderResDTO.setPaymentMethod(order.getPaymentMethod());
    orderResDTO.setPaymentStatus(order.getPaymentStatus());
    orderResDTO.setPaymentUrl(null);
    return orderResDTO;
  }

  @Override
  public OrderPaymentDTO createVnPayOrder(OrderReqDTO orderReqDTO, User user,
      HttpServletRequest request) {
    Order order = createOrder(orderReqDTO, user, PaymentMethod.VNPAY);
    String paymentUrl = vnPayService.createPaymentUrl(order, request);
    OrderPaymentDTO orderResDTO = new OrderPaymentDTO();
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

  @Override
  public List<OrderResDTO> getOrdersByUser() {
    User user = userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get());
    List<Order> orders = orderRepository.findByUser(user);

    // mapping order to order res dto
    return orders.stream().map(this::mapToOrderResDTO).collect(Collectors.toList());
  }

  private OrderResDTO mapToOrderResDTO(Order order) {
    boolean canReview = order.getStatus() == OrderStatus.DELIVERED;

    boolean isReviewed = order.getUser() != null && order.getLineItems().stream()
        .anyMatch(lineItem -> lineItem.getProductVariant().getProduct().getReviews().stream()
            .anyMatch(review -> review.getUser() != null && 
                      review.getUser().getId().equals(order.getUser().getId())));

    canReview = canReview && !isReviewed;

    return OrderResDTO.builder().id(order.getId()).code(order.getCode())
        .orderDate(order.getOrderDate()).status(order.getStatus())
        .paymentMethod(order.getPaymentMethod()).paymentStatus(order.getPaymentStatus())
        .total(order.getTotal()).shippingFee(order.getShippingFee()).discount(order.getDiscount())
        .finalTotal(order.getFinalTotal()).canReview(canReview).isReviewed(isReviewed)
        .lineItems(
            order.getLineItems().stream().map(this::mapToLineItemDTO).collect(Collectors.toList()))
        .build();
  }

  private OrderResDTO.LineItem mapToLineItemDTO(LineItem lineItem) {
    return OrderResDTO.LineItem.builder().id(lineItem.getId())
        .productName(lineItem.getProductVariant().getProduct().getName())
        .color(lineItem.getProductVariant().getColor()).size(lineItem.getProductVariant().getSize())
        .quantity(lineItem.getQuantity()).unitPrice(lineItem.getUnitPrice())
        .discount(lineItem.getDiscountAmount()).build();
  }

  @Override
  public List<OrderReviewDTO> getOrderReview(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    return order.getLineItems().stream().filter(lineItem -> lineItem.getReview() != null)
        .map(lineItem -> OrderReviewDTO.builder().lineItemId(lineItem.getId())
            .productName(lineItem.getProductVariant().getProduct().getName())
            .color(lineItem.getProductVariant().getColor())
            .size(lineItem.getProductVariant().getSize())
            .user(lineItem.getReview().getUser().getEmail())
            .createdAt(lineItem.getReview().getCreatedAt()).rating(lineItem.getReview().getRating())
            .description(lineItem.getReview().getDescription()).build())
        .collect(Collectors.toList());
  }

  @Override
  public List<OrderReviewDTO> getLineItemByOrderId(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    return order.getLineItems().stream().map(this::mapToOrderReviewDTO)
        .collect(Collectors.toList());
  }

  private OrderReviewDTO mapToOrderReviewDTO(LineItem lineItem) {
    return OrderReviewDTO.builder().lineItemId(lineItem.getId())
        .productName(lineItem.getProductVariant().getProduct().getName())
        .color(lineItem.getProductVariant().getColor()).size(lineItem.getProductVariant().getSize())
        .build();
  }

  private void checkOrderBelongsToCurrentUser(Order order) {
    User currentUser = userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND)));

    if (!order.getUser().getId().equals(currentUser.getId())) {
      throw new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND);
    }
  }

  @Override
  public OrderReviewReqDTO createOrderReview(OrderReviewReqDTO orderReviewReqDTO) {
    Order order = orderRepository.findById(orderReviewReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new BadRequestException(ErrorMessage.REVIEW_NOT_ALLOWED);
    }

    List<OrderReviewReqDTO.ReviewItem> reviewItems = orderReviewReqDTO.getReviewItems();
    List<Review> reviews = new ArrayList<>();
    for (OrderReviewReqDTO.ReviewItem reviewItem : reviewItems) {
      LineItem lineItem = order.getLineItems().stream()
          .filter(item -> item.getId().equals(reviewItem.getLineItemId())).findFirst()
          .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LINE_ITEM_NOT_FOUND));

      if (lineItem.getReview() != null) {
        throw new ResourceAlreadyExistException(ErrorMessage.REVIEW_ALREADY_EXISTS);
      }

      Review review = new Review();
      review.setUser(order.getUser());
      review.setProduct(lineItem.getProductVariant().getProduct());
      review.setRating(reviewItem.getRating().doubleValue());
      review.setDescription(reviewItem.getDescription());
      review.setLineItem(lineItem);
      reviews.add(review);
    }
    reviewRepository.saveAll(reviews);

    return orderReviewReqDTO;
  }

  @Override
  public OrderReviewReqDTO updateOrderReview(OrderReviewReqDTO orderReviewReqDTO) {
    Order order = orderRepository.findById(orderReviewReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    List<OrderReviewReqDTO.ReviewItem> reviewItems = orderReviewReqDTO.getReviewItems();
    List<Review> updatedReviews = new ArrayList<>();

    for (OrderReviewReqDTO.ReviewItem reviewItem : reviewItems) {
      LineItem lineItem = order.getLineItems().stream()
          .filter(item -> item.getId().equals(reviewItem.getLineItemId())).findFirst()
          .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LINE_ITEM_NOT_FOUND));

      Review review = reviewRepository.findByLineItemId(lineItem.getId())
          .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.REVIEW_NOT_FOUND));

      // Update review details
      review.setRating(reviewItem.getRating().doubleValue());
      review.setDescription(reviewItem.getDescription());

      updatedReviews.add(review);
    }

    reviewRepository.saveAll(updatedReviews);

    return orderReviewReqDTO;
  }

  @Override
  public OrderResDTO updateOrderStatus(OrderStatusReqDTO orderStatusReqDTO) {
    Order order = orderRepository.findById(orderStatusReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    OrderStatus newStatus = OrderStatus.valueOf(orderStatusReqDTO.getStatus().toUpperCase());

    if (order.getPaymentMethod() == PaymentMethod.VNPAY && order.getPaymentStatus() != PaymentStatus.SUCCESS) {
      throw new BadRequestException(ErrorMessage.PRE_PAYMENT_NOT_SUCCESS);
    }

    order.setStatus(newStatus);

    if (order.getPaymentMethod() == PaymentMethod.COD) {
      if (newStatus == OrderStatus.DELIVERED) {
        order.setPaymentStatus(PaymentStatus.SUCCESS);
      } else if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED) {
        order.setPaymentStatus(PaymentStatus.FAILED);
      } else {
        order.setPaymentStatus(PaymentStatus.PENDING);
      }
    }

    orderRepository.save(order);

    return mapToOrderResDTO(order);
  }

}
