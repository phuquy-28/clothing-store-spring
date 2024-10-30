package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
import com.example.clothingstore.exception.OrderCreationException;
import com.example.clothingstore.exception.ResourceAlreadyExistException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.service.OrderService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.repository.ReviewRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.clothingstore.service.strategy.PaymentStrategy;
import com.example.clothingstore.service.strategy.DeliveryStrategy;
import com.example.clothingstore.service.strategy.factory.PaymentStrategyFactory;
import com.example.clothingstore.service.strategy.factory.DeliveryStrategyFactory;
import com.example.clothingstore.enumeration.DeliveryMethod;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderRepository orderRepository;

  private final ProductVariantRepository productVariantRepository;

  private final UserService userService;

  private final ReviewRepository reviewRepository;

  private final PaymentStrategyFactory paymentStrategyFactory;

  private final DeliveryStrategyFactory deliveryStrategyFactory;

  @Override
  @Transactional
  @Retryable(value = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 5,
      backoff = @Backoff(delay = 200))
  public OrderPaymentDTO checkOut(OrderReqDTO orderReqDTO, User user, HttpServletRequest request) {
    // Validate product quantity first
    validateProductQuantity(orderReqDTO);
    
    // Create order without updating product quantity
    Order order = createOrder(orderReqDTO, user);

    // Process delivery
    DeliveryStrategy deliveryStrategy =
        deliveryStrategyFactory.getStrategy(order.getDeliveryMethod());
    deliveryStrategy.processDelivery(order);
    order.setShippingFee(deliveryStrategy.calculateShippingFee(order));

    // Calculate final total
    double total = order.getTotal() != null ? order.getTotal() : 0;
    double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0;
    double discount = order.getDiscount() != null ? order.getDiscount() : 0;
    order.setFinalTotal(total + shippingFee - discount);

    // Process payment
    PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(order.getPaymentMethod());
    OrderPaymentDTO paymentResult = paymentStrategy.processPayment(order, request);

    // If everything is successful, update product quantities and save order
    updateProductQuantities(order);
    orderRepository.save(order);

    return paymentResult;
  }

  private void validateProductQuantity(OrderReqDTO orderReqDTO) {
    for (OrderReqDTO.LineItemReqDTO lineItemDTO : orderReqDTO.getLineItems()) {
        ProductVariant productVariant = productVariantRepository.findById(lineItemDTO.getProductVariantId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PRODUCT_VARIANT_NOT_FOUND));

        if (productVariant.getQuantity() < lineItemDTO.getQuantity()) {
            throw new BadRequestException(String.format(ErrorMessage.NOT_ENOUGH_STOCK));
        }
    }
  }

  private void updateProductQuantities(Order order) {
    for (LineItem lineItem : order.getLineItems()) {
        ProductVariant productVariant = lineItem.getProductVariant();
        int newQuantity = productVariant.getQuantity() - lineItem.getQuantity().intValue();
        if (newQuantity < 0) {
            throw new BadRequestException(String.format(ErrorMessage.NOT_ENOUGH_STOCK));
        }
        productVariant.setQuantity(newQuantity);
        productVariantRepository.save(productVariant);
    }
  }

  private Order createOrder(OrderReqDTO orderReqDTO, User user) {
    Order order = new Order();
    order.setUser(user);
    order.setOrderDate(Instant.now());
    order.setCode(generateOrderCode());
    order.setNote(orderReqDTO.getNote());
    order.setPaymentMethod(PaymentMethod.valueOf(orderReqDTO.getPaymentMethod().toUpperCase()));
    order.setDeliveryMethod(DeliveryMethod.valueOf(orderReqDTO.getDeliveryMethod().toUpperCase()));

    // Set shipping information
    setShippingInformation(order, orderReqDTO.getShippingProfile());

    // Create line items without updating product quantity
    List<LineItem> lineItems = createLineItems(orderReqDTO, order);
    order.setLineItems(lineItems);
    order.setTotal(calculateTotal(lineItems));

    // Set initial status
    order.setStatus(OrderStatus.PENDING);
    order.setPaymentStatus(PaymentStatus.PENDING);

    return order;
  }

  private String generateOrderCode() {
    long timestamp = System.currentTimeMillis();
    return String.format("ORD-%d", timestamp);
  }

  @Recover
  public OrderPaymentDTO recoverCreateOrder(ObjectOptimisticLockingFailureException e, OrderReqDTO orderReqDTO) {
    log.error("Failed to create order after 5 attempts with order req dto: {}", orderReqDTO);
    throw new OrderCreationException(ErrorMessage.SYSTEM_BUSY);
  }

  @Recover 
  public OrderPaymentDTO recoverCreateOrder(BadRequestException e, OrderReqDTO orderReqDTO) {
    throw e;
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
            .anyMatch(review -> review.getUser() != null
                && review.getUser().getId().equals(order.getUser().getId())));

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

    if (order.getPaymentMethod() == PaymentMethod.VNPAY
        && order.getPaymentStatus() != PaymentStatus.SUCCESS) {
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

  private void setShippingInformation(Order order, ShippingProfileReqDTO shippingProfile) {
    Order.ShippingInformation shippingInfo = new Order.ShippingInformation();
    shippingInfo.setFirstName(shippingProfile.getFirstName());
    shippingInfo.setLastName(shippingProfile.getLastName());
    shippingInfo.setPhoneNumber(shippingProfile.getPhoneNumber());
    shippingInfo.setAddress(shippingProfile.getAddress());
    shippingInfo.setDistrict(shippingProfile.getDistrict());
    shippingInfo.setProvince(shippingProfile.getProvince());
    shippingInfo.setCountry(shippingProfile.getCountry());
    order.setShippingInformation(shippingInfo);
  }

  private List<LineItem> createLineItems(OrderReqDTO orderReqDTO, Order order) {
    List<LineItem> lineItems = new ArrayList<>();
    
    for (OrderReqDTO.LineItemReqDTO lineItemDTO : orderReqDTO.getLineItems()) {
        ProductVariant productVariant = productVariantRepository.findById(lineItemDTO.getProductVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("productVariant.not.found"));

        LineItem lineItem = new LineItem();
        lineItem.setProductVariant(productVariant);
        lineItem.setQuantity(lineItemDTO.getQuantity().longValue());
        lineItem.setUnitPrice(productVariant.getProduct().getPrice() + productVariant.getDifferencePrice());
        lineItem.setTotalPrice(lineItem.getUnitPrice() * lineItem.getQuantity());
        lineItem.setOrder(order);
        
        lineItems.add(lineItem);
    }
    
    return lineItems;
  }

  private double calculateTotal(List<LineItem> lineItems) {
    return lineItems.stream()
        .mapToDouble(LineItem::getTotalPrice)
        .sum();
  }

}
