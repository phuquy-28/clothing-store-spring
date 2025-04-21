package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import lombok.RequiredArgsConstructor;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.OrderPreviewReqDTO;
import com.example.clothingstore.dto.request.OrderReqDTO;
import com.example.clothingstore.dto.request.OrderReviewReqDTO;
import com.example.clothingstore.dto.request.OrderStatusReqDTO;
import com.example.clothingstore.dto.response.CartItemDTO;
import com.example.clothingstore.dto.response.OrderDetailsDTO;
import com.example.clothingstore.dto.response.OrderItemList;
import com.example.clothingstore.dto.response.OrderPaymentDTO;
import com.example.clothingstore.dto.response.OrderPreviewDTO;
import com.example.clothingstore.dto.response.OrderResDTO;
import com.example.clothingstore.dto.response.OrderReviewDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ShippingProfileResDTO;
import com.example.clothingstore.entity.Cart;
import com.example.clothingstore.entity.CartItem;
import com.example.clothingstore.entity.LineItem;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.Review;
import com.example.clothingstore.entity.ShippingProfile;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.OrderCreationException;
import com.example.clothingstore.exception.ResourceAlreadyExistException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.CartRepository;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.service.OderCancellationService;
import com.example.clothingstore.service.OrderService;
import com.example.clothingstore.service.PromotionCalculatorService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.repository.ReviewRepository;
import com.example.clothingstore.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.clothingstore.service.strategy.PaymentStrategy;
import com.example.clothingstore.service.strategy.DeliveryStrategy;
import com.example.clothingstore.service.strategy.factory.PaymentStrategyFactory;
import com.example.clothingstore.service.strategy.factory.DeliveryStrategyFactory;
import com.example.clothingstore.enumeration.DeliveryMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.temporal.ChronoUnit;
import com.example.clothingstore.service.PointService;
import com.example.clothingstore.entity.Point;
import com.example.clothingstore.entity.PointHistory;
import com.example.clothingstore.enumeration.PointActionType;
import com.example.clothingstore.repository.PointRepository;
import com.example.clothingstore.repository.PointHistoryRepository;
import com.example.clothingstore.service.NotificationService;

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

  private final UserRepository userRepository;

  private final PromotionCalculatorService promotionCalculatorService;

  private final CartRepository cartRepository;

  private final OderCancellationService oderCancellationService;

  private final PointService pointService;

  private final PointRepository pointRepository;

  private final PointHistoryRepository pointHistoryRepository;

  private final NotificationService notificationService;

  @Override
  @Transactional
  @Retryable(value = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 5,
      backoff = @Backoff(delay = 500))
  public OrderPaymentDTO checkOut(OrderReqDTO orderReqDTO, User user, HttpServletRequest request) {
    // Lấy shipping profile
    ShippingProfile shippingProfile = user.getShippingProfiles().stream()
        .filter(profile -> profile.getId().equals(orderReqDTO.getShippingProfileId()))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND));

    Cart cart = user.getCart();
    if (cart == null) {
        throw new ResourceNotFoundException(ErrorMessage.CART_NOT_FOUND);
    }

    // Tạo map để lưu cartItem theo id để dễ truy xuất
    Map<Long, CartItem> cartItemMap = cart.getCartItems().stream()
        .collect(Collectors.toMap(CartItem::getId, item -> item));

    // Validate product quantity và tạo line items
    List<LineItem> lineItems = new ArrayList<>();
    double total = 0.0;
    double discount = 0.0;

    for (Long cartItemId : orderReqDTO.getCartItemIds()) {
        CartItem cartItem = cartItemMap.get(cartItemId);
        if (cartItem == null) {
            throw new ResourceNotFoundException(ErrorMessage.CART_ITEM_NOT_FOUND);
        }

        ProductVariant variant = cartItem.getProductVariant();
        // Kiểm tra số lượng tồn kho
        if (variant.getQuantity() < cartItem.getQuantity()) {
            throw new BadRequestException(ErrorMessage.NOT_ENOUGH_STOCK);
        }

        Product product = variant.getProduct();
        Double originalPrice = product.getPrice() + 
            (variant.getDifferencePrice() != null ? variant.getDifferencePrice() : 0.0);
        Double discountRate = promotionCalculatorService.calculateDiscountRate(product);
        Double finalPrice = originalPrice * (1 - discountRate);

        // Tạo line item
        LineItem lineItem = new LineItem();
        lineItem.setProductVariant(variant);
        lineItem.setQuantity(cartItem.getQuantity().longValue());
        lineItem.setUnitPrice(originalPrice);
        lineItem.setDiscountAmount(originalPrice - finalPrice);
        lineItem.setFinalPrice(finalPrice);

        lineItems.add(lineItem);
        total += originalPrice * cartItem.getQuantity();
        discount += (originalPrice - finalPrice) * cartItem.getQuantity();

        // Cập nhật số lượng tồn kho
        variant.setQuantity(variant.getQuantity() - cartItem.getQuantity());
        productVariantRepository.save(variant);
    }

    // Tính khuyến mãi từ điểm thưởng nếu có
    Double pointDiscount = 0.0;
    Long pointsToUse = 0L;
    PointHistory pointHistory = null;
    String orderCode = generateOrderCode();

    if (orderReqDTO.getIsUsePoint()) {
        Point userPoint = user.getPoint();
        if (userPoint != null && userPoint.getCurrentPoints() > 0) {
            // Tính số tiền tối đa có thể giảm từ điểm
            Long availablePoints = userPoint.getCurrentPoints();
            pointDiscount = Math.min(total - discount, pointService.calculateAmountFromPoints(availablePoints));
            
            // Tính số điểm cần dùng dựa trên số tiền tối đa có thể giảm
            pointsToUse = pointDiscount.longValue(); // POINT_REDEMPTION_RATE = 1

            // Cập nhật số điểm của người dùng
            userPoint.setCurrentPoints(userPoint.getCurrentPoints() - pointsToUse);
            pointRepository.save(userPoint);

            // Ghi lại lịch sử sử dụng điểm
            pointHistory = new PointHistory();
            pointHistory.setUser(user);
            pointHistory.setPoints(-pointsToUse);
            pointHistory.setActionType(PointActionType.USED);
            pointHistory.setDescription(String.format("Sử dụng %d điểm để giảm giá %.0f VND cho đơn hàng %s", 
                pointsToUse, pointDiscount, orderCode));
            pointHistoryRepository.save(pointHistory);

            log.debug("Người dùng {} đã sử dụng {} điểm để giảm giá {} VND cho đơn hàng", 
                user.getEmail(), pointsToUse, pointDiscount);
        }
    }

    // Tạo order
    Order order = new Order();
    order.setUser(user);
    order.setOrderDate(Instant.now());
    order.setCode(orderCode);
    order.setNote(orderReqDTO.getNote());
    order.setPaymentMethod(PaymentMethod.valueOf(orderReqDTO.getPaymentMethod().toUpperCase()));
    order.setDeliveryMethod(DeliveryMethod.valueOf(orderReqDTO.getDeliveryMethod().toUpperCase()));
    order.setShippingInformation(mapToShippingInformation(shippingProfile));
    order.setLineItems(lineItems);
    order.setTotal(total);
    order.setDiscount(discount);
    order.setPointsUsed(pointsToUse > 0 ? pointsToUse : null);  // Lưu số điểm đã sử dụng
    order.setPointDiscount(pointDiscount > 0 ? pointDiscount : null);  // Lưu số tiền giảm giá từ điểm
    if (pointHistory != null) {
      order.getPointHistories().add(pointHistory);
    }

    // Set initial status
    order.setStatus(OrderStatus.PENDING);
    order.setPaymentStatus(PaymentStatus.PENDING);

    // Process delivery
    DeliveryStrategy deliveryStrategy = 
        deliveryStrategyFactory.getStrategy(order.getDeliveryMethod());
    Double shippingFee = deliveryStrategy.calculateShippingFee(order);
    // deliveryStrategy.processDelivery(order);
    order.setShippingFee(shippingFee);

    // Calculate final total (bao gồm cả giảm giá từ điểm)
    double finalTotal = total + order.getShippingFee() - discount - pointDiscount;
    order.setFinalTotal(finalTotal >= 0 ? finalTotal : 0);

    // Set bi-directional relationship
    lineItems.forEach(lineItem -> lineItem.setOrder(order));

    // Process payment
    PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(order.getPaymentMethod());
    OrderPaymentDTO paymentResult = paymentStrategy.processPayment(order, request);

    // Save order
    Order savedOrder = orderRepository.save(order);

    paymentResult.setOrderId(savedOrder.getId());

    // Clear cart items that were ordered
    orderReqDTO.getCartItemIds().forEach(cartItemId -> {
        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
    });
    cartRepository.save(cart);

    return paymentResult;
  }

  private Order.ShippingInformation mapToShippingInformation(ShippingProfile profile) {
    Order.ShippingInformation shippingInfo = new Order.ShippingInformation();
    shippingInfo.setFirstName(profile.getFirstName());
    shippingInfo.setLastName(profile.getLastName());
    shippingInfo.setFullName(String.format("%s %s", profile.getLastName(), profile.getFirstName()));
    shippingInfo.setPhoneNumber(profile.getPhoneNumber());
    shippingInfo.setAddress(profile.getAddress());
    shippingInfo.setWardId(profile.getWardId());
    shippingInfo.setWard(profile.getWard());
    shippingInfo.setDistrictId(profile.getDistrictId());
    shippingInfo.setDistrict(profile.getDistrict());
    shippingInfo.setProvinceId(profile.getProvinceId());
    shippingInfo.setProvince(profile.getProvince());
    return shippingInfo;
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

  @Recover
  public OrderPaymentDTO recoverCreateOrder(ResourceNotFoundException e, OrderReqDTO orderReqDTO) {
    throw e;
  }

  @Override
  public ResultPaginationDTO getOrdersByUser(Specification<Order> spec, Pageable pageable) {
    User currentUser = userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get());
    
    Specification<Order> userSpec = (root, query, cb) -> 
        cb.equal(root.get("user").get("id"), currentUser.getId());
    
    Specification<Order> finalSpec = spec != null ? userSpec.and(spec) : userSpec;
    
    Page<Order> orderPage = orderRepository.findAll(finalSpec, pageable);
    
    List<OrderResDTO> orderDTOs = orderPage.getContent().stream()
        .map(this::mapToOrderResDTO)
        .collect(Collectors.toList());

    return ResultPaginationDTO.builder()
        .meta(ResultPaginationDTO.Meta.builder()
            .page(Long.valueOf(pageable.getPageNumber()))
            .pageSize(Long.valueOf(pageable.getPageSize()))
            .total(orderPage.getTotalElements())
            .pages(Long.valueOf(orderPage.getTotalPages()))
            .build())
        .data(orderDTOs)
        .build();
  }

  private OrderResDTO mapToOrderResDTO(Order order) {
    boolean canReview = order.getStatus() == OrderStatus.DELIVERED;

    boolean isReviewed = order.getLineItems().stream()
        .anyMatch(lineItem -> lineItem.getProductVariant().getProduct().getReviews().stream()
            .anyMatch(review -> review.getLineItem().getId().equals(lineItem.getId())));

    canReview = canReview && !isReviewed;

    return OrderResDTO.builder().id(order.getId()).code(order.getCode())
        .orderDate(order.getOrderDate()).status(order.getStatus())
        .paymentMethod(order.getPaymentMethod()).paymentStatus(order.getPaymentStatus())
        .total(order.getTotal()).shippingFee(order.getShippingFee()).discount(order.getDiscount())
        .finalTotal(order.getFinalTotal()).canReview(canReview).isReviewed(isReviewed)
        .cancelReason(order.getCancelReason())
        .lineItems(
            order.getLineItems().stream().map(this::mapToLineItemDTO).collect(Collectors.toList()))
        .build();
  }

  private OrderResDTO.LineItem mapToLineItemDTO(LineItem lineItem) {
    return OrderResDTO.LineItem.builder().id(lineItem.getId())
        .productName(lineItem.getProductVariant().getProduct().getName())
        .color(lineItem.getProductVariant().getColor()).size(lineItem.getProductVariant().getSize())
        .variantImage(lineItem.getProductVariant().getImages().get(0).getPublicUrl())
        .quantity(lineItem.getQuantity()).unitPrice(lineItem.getUnitPrice())
        .discount(lineItem.getDiscountAmount()).build();
  }

  @Override
  public List<OrderReviewDTO> getOrderReview(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    return order.getLineItems().stream()
        .map(lineItem -> OrderReviewDTO.builder()
            .lineItemId(lineItem.getId())
            .productName(lineItem.getProductVariant().getProduct().getName())
            .color(lineItem.getProductVariant().getColor())
            .size(lineItem.getProductVariant().getSize())
            .variantImage(lineItem.getProductVariant().getImages().get(0).getPublicUrl())
            .firstName(lineItem.getReview() != null ? lineItem.getReview().getUser().getProfile().getFirstName() : null)
            .lastName(lineItem.getReview() != null ? lineItem.getReview().getUser().getProfile().getLastName() : null)
            .avatar(lineItem.getReview() != null ? lineItem.getReview().getUser().getProfile().getAvatar() : null)
            .createdAt(lineItem.getReview() != null ? lineItem.getReview().getCreatedAt() : null)
            .rating(lineItem.getReview() != null ? lineItem.getReview().getRating() : null)
            .description(lineItem.getReview() != null ? lineItem.getReview().getDescription() : null)
            .build())
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
  @Transactional
  public OrderReviewReqDTO createOrderReview(OrderReviewReqDTO orderReviewReqDTO) {
    Order order = orderRepository.findById(orderReviewReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    if (order.getStatus() != OrderStatus.DELIVERED) {
        throw new BadRequestException(ErrorMessage.REVIEW_NOT_ALLOWED);
    }

    OrderReviewReqDTO.ReviewItem reviewItem = orderReviewReqDTO.getReviewItem();
    
    LineItem lineItem = order.getLineItems().stream()
        .filter(item -> item.getId().equals(reviewItem.getLineItemId()))
        .findFirst()
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
    reviewRepository.save(review);

    pointService.addPointsFromOrderReview(review);

    reviewRepository.save(review);

    return orderReviewReqDTO;
  }

  @Override
  public OrderReviewReqDTO updateOrderReview(OrderReviewReqDTO orderReviewReqDTO) {
    Order order = orderRepository.findById(orderReviewReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));
    checkOrderBelongsToCurrentUser(order);

    OrderReviewReqDTO.ReviewItem reviewItem = orderReviewReqDTO.getReviewItem();

    LineItem lineItem = order.getLineItems().stream()
        .filter(item -> item.getId().equals(reviewItem.getLineItemId()))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LINE_ITEM_NOT_FOUND));

    Review review = reviewRepository.findByLineItemId(lineItem.getId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.REVIEW_NOT_FOUND));

    review.setRating(reviewItem.getRating().doubleValue());
    review.setDescription(reviewItem.getDescription());
    
    reviewRepository.save(review);

    return orderReviewReqDTO;
  }

  @Override
  public OrderItemList updateOrderStatus(OrderStatusReqDTO orderStatusReqDTO) {
    Order order = orderRepository.findById(orderStatusReqDTO.getOrderId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    OrderStatus oldStatus = order.getStatus();
    OrderStatus newStatus = OrderStatus.valueOf(orderStatusReqDTO.getStatus().toUpperCase());

    // Kiểm tra logic chuyển trạng thái
    if (oldStatus == OrderStatus.DELIVERED) {
      // Nếu đơn hàng đã ở trạng thái DELIVERED, chỉ cho phép chuyển sang RETURNED
      if (newStatus != OrderStatus.RETURNED) {
        throw new BadRequestException(ErrorMessage.ORDER_CAN_ONLY_BE_RETURNED);
      }
    } else if (newStatus == OrderStatus.RETURNED) {
      // Nếu đơn hàng chưa giao (chưa DELIVERED), không cho phép chuyển sang RETURNED
      throw new BadRequestException(ErrorMessage.ORDER_CAN_NOT_BE_RETURNED);
    }

    // Kiểm tra thanh toán VNPAY
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
        if (orderStatusReqDTO.getReason() != null && !orderStatusReqDTO.getReason().isEmpty()) {
          order.setCancelReason(orderStatusReqDTO.getReason());
        }
      } else {
        order.setPaymentStatus(PaymentStatus.PENDING);
      }
    }

    // Xử lý điểm thưởng
    if (newStatus == OrderStatus.DELIVERED && oldStatus != OrderStatus.DELIVERED) {
      pointService.addPointsFromOrder(order);
    } else if ((newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED)
        && oldStatus == OrderStatus.DELIVERED) {
      pointService.refundPointsFromOrder(order);
    }

    orderRepository.save(order);

    // Create and send notification
    notificationService.createOrderStatusNotification(order);

    return mapToOrderItemList(order);
  }

  @Override
  public OrderPreviewDTO previewOrder(OrderPreviewReqDTO orderPreviewReqDTO) {
    // Lấy thông tin user hiện tại
    String email = SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new BadRequestException(ErrorMessage.USER_NOT_LOGGED_IN));
    
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    // Lấy shipping profile
    ShippingProfile shippingProfile;
    if (orderPreviewReqDTO.getShippingProfileId() != null) {
        shippingProfile = user.getShippingProfiles().stream()
            .filter(profile -> profile.getId().equals(orderPreviewReqDTO.getShippingProfileId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND));
    } else {
        shippingProfile = user.getDefaultShippingProfile() != null ? user.getDefaultShippingProfile() : null;
    }

    Cart cart = user.getCart();
    if (cart == null) {
        throw new ResourceNotFoundException(ErrorMessage.CART_NOT_FOUND);
    }

    // Tạo map để lưu cartItem theo id để dễ truy xuất
    Map<Long, CartItem> cartItemMap = cart.getCartItems().stream()
        .collect(Collectors.toMap(CartItem::getId, item -> item));

    // Tính toán các sản phẩm được chọn
    List<CartItemDTO> selectedItems = new ArrayList<>();
    double subtotal = 0.0;
    double discount = 0.0;

    for (Long cartItemId : orderPreviewReqDTO.getCartItemIds()) {
        CartItem cartItem = cartItemMap.get(cartItemId);
        if (cartItem == null) {
            throw new ResourceNotFoundException(ErrorMessage.CART_ITEM_NOT_FOUND);
        }

        // Kiểm tra số lượng tồn kho
        if (cartItem.getProductVariant().getQuantity() < cartItem.getQuantity()) {
            throw new BadRequestException(ErrorMessage.NOT_ENOUGH_STOCK);
        }

        ProductVariant variant = cartItem.getProductVariant();
        Product product = variant.getProduct();

        // Tính giá gốc và giá sau khuyến mãi
        Double originalPrice = product.getPrice() + 
            (variant.getDifferencePrice() != null ? variant.getDifferencePrice() : 0.0);
        Double discountRate = promotionCalculatorService.calculateDiscountRate(product);
        Double finalPrice = originalPrice * (1 - discountRate);

        // Tính tổng tiền và discount cho item này
        subtotal += originalPrice * cartItem.getQuantity();
        discount += (originalPrice - finalPrice) * cartItem.getQuantity();

        selectedItems.add(CartItemDTO.builder()
            .cartItemId(cartItem.getId())
            .productName(product.getName())
            .productVariant(CartItemDTO.ProductVariantDTO.builder()
                .id(variant.getId())
                .color(variant.getColor().toString())
                .size(variant.getSize().toString())
                .image(variant.getImages().get(0).getPublicUrl())
                .build())
            .price(originalPrice)
            .discountRate(discountRate)
            .finalPrice(finalPrice)
            .quantity(cartItem.getQuantity())
            .inStock(variant.getQuantity())
            .image(product.getImages().get(0).getPublicUrl())
            .build());
    }

    // Tính khuyến mãi từ điểm thưởng nếu có
    Double pointDiscount = 0.0;

    if (orderPreviewReqDTO.getIsUsePoint()) {
      // Lấy số điểm hiện có của user
      Long availablePoints = user.getPoint() == null ? 0L : user.getPoint().getCurrentPoints();

      // Tính số tiền tối đa có thể giảm từ điểm
      pointDiscount = Math.min(subtotal, pointService.calculateAmountFromPoints(availablePoints));
    }

    // Tính phí vận chuyển
    DeliveryMethod deliveryMethod = DeliveryMethod.valueOf(orderPreviewReqDTO.getDeliveryMethod());
    DeliveryStrategy deliveryStrategy = deliveryStrategyFactory.getStrategy(deliveryMethod);
    Double shippingFee = null;
    if (shippingProfile != null) {
      // create a temp order to pass to the strategy
      Order tempOrder = new Order();
      tempOrder.setTotal(subtotal);
      tempOrder.setDiscount(discount);
      tempOrder.setShippingInformation(mapToShippingInformation(shippingProfile));
      tempOrder.setPaymentMethod(PaymentMethod.valueOf(orderPreviewReqDTO.getPaymentMethod().toUpperCase()));
      shippingFee = deliveryStrategy.calculateShippingFee(tempOrder);
    } else {
      shippingFee = null;
    }

    // Tạo preview order
    return OrderPreviewDTO.builder()
        .shippingProfile(shippingProfile != null ? ShippingProfileResDTO.builder()
            .id(shippingProfile.getId())
            .firstName(shippingProfile.getFirstName())
            .lastName(shippingProfile.getLastName())
            .phoneNumber(shippingProfile.getPhoneNumber())
            .address(shippingProfile.getAddress())
            .ward(shippingProfile.getWard())
            .district(shippingProfile.getDistrict())
            .province(shippingProfile.getProvince())
            .build() : null)
        .lineItems(selectedItems)
        .shippingFee(shippingFee)
        .discount(discount)
        .pointDiscount(pointDiscount)
        .finalTotal(subtotal + (shippingFee != null ? shippingFee : 0.0) - discount - pointDiscount)
        .points(user.getPoint() == null ? 0L : user.getPoint().getCurrentPoints())
        .build();
  }

  @Override
  public ResultPaginationDTO getOrders(Specification<Order> spec, Pageable pageable) {
    
    Page<Order> orderPage = orderRepository.findAll(spec, pageable);

    List<OrderItemList> orderItemList = orderPage.getContent().stream()
        .map(this::mapToOrderItemList)
        .collect(Collectors.toList());

    return ResultPaginationDTO.builder()
        .meta(ResultPaginationDTO.Meta.builder()
            .page(Long.valueOf(pageable.getPageNumber()))
            .pageSize(Long.valueOf(pageable.getPageSize()))
            .total(orderPage.getTotalElements())
            .pages(Long.valueOf(orderPage.getTotalPages()))
            .build())
        .data(orderItemList)
        .build();
  }

  private OrderItemList mapToOrderItemList(Order order) {
    return OrderItemList.builder()
        .id(order.getId())
        .orderCode(order.getCode())
        .orderDate(LocalDateTime.ofInstant(order.getOrderDate(), ZoneId.systemDefault()))
        .customerName(order.getUser().getProfile().getFirstName() + " " 
            + order.getUser().getProfile().getLastName())
        .total(order.getTotal())
        .paymentStatus(order.getPaymentStatus())
        .orderStatus(order.getStatus())
        .numberOfItems(order.getLineItems().stream().mapToLong(LineItem::getQuantity).sum())
        .paymentMethod(order.getPaymentMethod())
        .deliveryMethod(order.getDeliveryMethod())
        .build();
  }

  @Override
  public OrderPaymentDTO continuePayment(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    if (order.getPaymentMethod() != PaymentMethod.VNPAY) {
        throw new BadRequestException(ErrorMessage.PAYMENT_METHOD_NOT_SUPPORTED);
    }

    if (order.getPaymentStatus() != PaymentStatus.PENDING) {
        throw new BadRequestException(ErrorMessage.PAYMENT_STATUS_NOT_SUPPORTED);
    }

    if (order.getStatus() != OrderStatus.PENDING) {
        throw new BadRequestException(ErrorMessage.ORDER_STATUS_NOT_SUPPORTED);
    }

    Instant thirtyMinutesAgo = Instant.now().minus(30, ChronoUnit.MINUTES);
    if (order.getOrderDate().isBefore(thirtyMinutesAgo)) {
        oderCancellationService.cancelOrderAndReturnStock(orderId);
        throw new BadRequestException(ErrorMessage.ORDER_EXPIRED);
    }

    String currentUserEmail = SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new BadRequestException(ErrorMessage.USER_NOT_LOGGED_IN));
    if (!order.getUser().getEmail().equals(currentUserEmail)) {
        throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    try {
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(PaymentMethod.VNPAY);
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();

        return paymentStrategy.processPayment(order, request);
    } catch (Exception e) {
        log.error("Lỗi khi xử lý thanh toán: {}", e.getMessage());
        throw new BadRequestException(ErrorMessage.TRANSACTION_FAILED);
    }
  }

  @Override
  public OrderDetailsDTO getOrderDetailsUser(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    // check if the order belongs to the current user
    User currentUser = userService.handleGetUserByUsername(SecurityUtil.getCurrentUserLogin().get());
    if (!order.getUser().getId().equals(currentUser.getId())) {
        throw new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND);
    }

    return mapToOrderDetailsDTO(order);
  }

  private OrderDetailsDTO mapToOrderDetailsDTO(Order order) {
    // Check if order can be reviewed
    boolean canReview = order.getStatus() == OrderStatus.DELIVERED;
    
    // Check if order is already reviewed
    boolean isReviewed = order.getLineItems().stream()
        .anyMatch(lineItem -> lineItem.getProductVariant().getProduct().getReviews().stream()
            .anyMatch(review -> review.getLineItem().getId().equals(lineItem.getId())));

    canReview = canReview && !isReviewed;

    // Map shipping information to ShippingProfileResDTO
    ShippingProfileResDTO shippingProfile = ShippingProfileResDTO.builder()
        .firstName(order.getShippingInformation().getFirstName())
        .lastName(order.getShippingInformation().getLastName())
        .phoneNumber(order.getShippingInformation().getPhoneNumber())
        .address(order.getShippingInformation().getAddress())
        .ward(order.getShippingInformation().getWard())
        .district(order.getShippingInformation().getDistrict())
        .province(order.getShippingInformation().getProvince())
        .build();

    // Map line items
    List<OrderDetailsDTO.LineItem> lineItems = order.getLineItems().stream()
        .map(item -> OrderDetailsDTO.LineItem.builder()
            .id(item.getId())
            .productName(item.getProductVariant().getProduct().getName())
            .color(item.getProductVariant().getColor())
            .size(item.getProductVariant().getSize())
            .variantImage(item.getProductVariant().getImages().get(0).getPublicUrl())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .discount(item.getDiscountAmount())
            .build())
        .collect(Collectors.toList());

    // Build and return OrderDetailsDTO
    return OrderDetailsDTO.builder()
        .id(order.getId())
        .code(order.getCode())
        .orderDate(order.getOrderDate())
        .status(order.getStatus())
        .paymentMethod(order.getPaymentMethod())
        .paymentStatus(order.getPaymentStatus())
        .paymentDate(order.getPaymentDate())
        .deliveryMethod(order.getDeliveryMethod())
        .lineItems(lineItems)
        .total(order.getTotal())
        .shippingFee(order.getShippingFee())
        .discount(order.getDiscount())
        .pointDiscount(order.getPointDiscount())
        .finalTotal(order.getFinalTotal())
        .canReview(canReview)
        .isReviewed(isReviewed)
        .cancelReason(order.getCancelReason())
        .shippingProfile(shippingProfile)
        .build();
  }

  @Override
  public OrderDetailsDTO getOrderDetails(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    return mapToOrderDetailsDTO(order);
  }
}
