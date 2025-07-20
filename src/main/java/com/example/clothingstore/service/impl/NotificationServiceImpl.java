package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.NotificationReqDTO;
import com.example.clothingstore.dto.response.NotificationResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Notification;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.entity.ScheduledNotification;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.entity.UserDevice;
import com.example.clothingstore.enumeration.NotificationType;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.NotificationRepository;
import com.example.clothingstore.repository.PromotionRepository;
import com.example.clothingstore.repository.ScheduledNotificationRepository;
import com.example.clothingstore.repository.UserDeviceRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.NotificationService;
import com.example.clothingstore.util.SecurityUtil;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
  private final FirebaseMessaging firebaseMessaging;
  private final UserDeviceRepository userDeviceRepository;

  private final NotificationRepository notificationRepository;
  private final ScheduledNotificationRepository scheduledNotificationRepository;
  private final PromotionRepository promotionRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final SecurityUtil securityUtil;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public NotificationResDTO createOrderStatusNotification(Order order) {
    User user = order.getUser();

    // Create notification content based on order status
    String title = "Cập nhật trạng thái đơn hàng";
    String content = String.format("Đơn hàng #%s của bạn đã được cập nhật thành: %s", order.getCode(),
        getStatusDisplay(order.getStatus()));

    // Create and save notification
    Notification notification = new Notification();
    notification.setUser(user);
    notification.setTitle(title);
    notification.setContent(content);
    notification.setType(NotificationType.ORDER_STATUS_UPDATED);
    notification.setReferenceIds(order.getId().toString());
    notification.setNotificationDate(Instant.now());
    notification = notificationRepository.save(notification);

    // Convert to DTO for sending via WebSocket
    NotificationResDTO notificationDTO = convertToNotificationDTO(notification);

    return notificationDTO;
  }

  @Override
  @Transactional
  public NotificationResDTO createNewOrderNotification(Order order) {
    // Find all admin, manager and staff users
    List<User> adminUsers = userRepository.findByRoleNameIn(Arrays.asList(
        AppConstant.ROLE_ADMIN,
        AppConstant.ROLE_MANAGER,
        AppConstant.ROLE_STAFF
    ));

    String title = "Đơn hàng mới";
    String formattedAmount = String.format("%,.0f", order.getFinalTotal()).replace(",", ".");
    String content = String.format("Đơn hàng mới #%s đã được tạo với tổng giá trị %s VND", 
        order.getCode(), formattedAmount);

    List<Notification> notifications = new ArrayList<>();

    // Create notification for each admin user
    for (User adminUser : adminUsers) {
      Notification notification = new Notification();
      notification.setUser(adminUser);
      notification.setTitle(title);
      notification.setContent(content);
      notification.setType(NotificationType.SYSTEM_NOTIFICATION);
      notification.setReferenceIds(order.getId().toString());
      notification.setNotificationDate(Instant.now());
      notifications.add(notification);
    }

    // Save all notifications
    List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

    // Create notification DTO for broadcasting
    NotificationResDTO notificationDTO = null;
    if (!savedNotifications.isEmpty()) {
      notificationDTO = convertToNotificationDTO(savedNotifications.get(0));
    }

    // Broadcast notification to admin topic
    if (notificationDTO != null) {
      log.debug("Broadcast notification to admin topic: {}", notificationDTO);
      messagingTemplate.convertAndSend("/topic/admin-notifications", notificationDTO);
    }

    return notificationDTO;
  }

  @Override
  @Transactional
  public NotificationResDTO.PromotionNotificationDTO createPromotionNotification(
      NotificationReqDTO.CreatePromotionNotificationDTO dto) {

    // Find promotion
    Promotion promotion = promotionRepository.findById(dto.getPromotionId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROMOTION_NOT_FOUND));

    // Get product IDs (if available)
    HashSet<Long> productIds =
        new HashSet<>(promotion.getProducts().stream().map(Product::getId).toList());
    promotion.getCategories().forEach(category -> productIds
        .addAll(category.getProducts().stream().map(Product::getId).toList()));

    // If scheduled, create a scheduled notification
    if (dto.getScheduledDate() != null) {
      ScheduledNotification scheduledNotification = new ScheduledNotification();
      scheduledNotification.setTitle(dto.getTitle());
      scheduledNotification.setContent(dto.getContent());
      scheduledNotification.setType(NotificationType.PROMOTION_NOTIFICATION);
      scheduledNotification.setReferenceIds(promotion.getId().toString());
      scheduledNotification.setScheduledDate(dto.getScheduledDate().toInstant(ZoneOffset.UTC));
      scheduledNotification.setSent(false);
      scheduledNotification.setStartPromotionDate(promotion.getStartDate());
      scheduledNotification.setEndPromotionDate(promotion.getEndDate());

      ScheduledNotification savedScheduledNotification =
          scheduledNotificationRepository.save(scheduledNotification);

      // Create DTO with promotion products
      NotificationResDTO.PromotionNotificationDTO notificationDTO =
          createPromotionNotificationDTO(savedScheduledNotification, promotion);

      return notificationDTO;
    } else {
      // If not scheduled, create and broadcast immediately
      NotificationResDTO.PromotionNotificationDTO notificationDTO =
          createPromotionNotificationDTO(null, promotion);

      // Create notifications for all users
      createNotificationsForAllUsers(notificationDTO, productIds);

      // Send broadcast notification
      sendBroadcastNotification(notificationDTO);

      return notificationDTO;
    }
  }

  @Override
  public ResultPaginationDTO getUserNotifications(Pageable pageable) {
    User currentUser = securityUtil.getCurrentUser();
    
    // Get paginated notifications
    Page<Notification> notificationsPage = notificationRepository.findByUserOrderByNotificationDateDesc(currentUser, pageable);
    
    // Convert notifications to DTOs
    List<NotificationResDTO> notifications = notificationsPage.getContent()
        .stream()
        .map(this::convertToNotificationDTO)
        .collect(Collectors.toList());

    // Build meta information
    ResultPaginationDTO.Meta meta = ResultPaginationDTO.Meta.builder()
        .page((long) pageable.getPageNumber())
        .pageSize((long) pageable.getPageSize())
        .total(notificationsPage.getTotalElements())
        .pages((long) notificationsPage.getTotalPages())
        .build();

    // Build final response
    return ResultPaginationDTO.builder()
        .meta(meta)
        .data(notifications)
        .build();
  }

  @Override
  public long getUnreadNotificationCount() {
    User currentUser = securityUtil.getCurrentUser();
    return notificationRepository.countByUserAndReadFalse(currentUser);
  }

  @Override
  @Transactional
  public NotificationResDTO markNotificationAsRead(Long notificationId) {
    User currentUser = securityUtil.getCurrentUser();

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND));

    // Verify the notification belongs to the current user
    if (notification.getUser() != null
        && !notification.getUser().getId().equals(currentUser.getId())) {
      throw new ResourceNotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND);
    }

    notification.setRead(true);
    notification = notificationRepository.save(notification);

    return convertToNotificationDTO(notification);
  }

  @Async
  @Override
  public void sendNotificationToUser(User user, NotificationResDTO notification) {
    String destination = "/queue/notifications";
    messagingTemplate.convertAndSendToUser(user.getEmail(), destination, notification);
    log.debug("Sent notification to user {}: {}", user.getEmail(), notification);
  }

  @Override
  public void sendBroadcastNotification(NotificationResDTO.PromotionNotificationDTO notification) {
    messagingTemplate.convertAndSend("/topic/promotions", notification);
    log.debug("Broadcast promotion notification: {}", notification);
  }

  @Override
  @Transactional
  public void createNotificationsForAllUsers(
      NotificationResDTO.PromotionNotificationDTO notification, HashSet<Long> productIds) {
    // Convert product IDs to comma-separated string
    String referenceIds = productIds != null && !productIds.isEmpty()
        ? String.join(",", productIds.stream().map(String::valueOf).collect(Collectors.toList()))
        : null;

    // Only get users with USER role
    List<Notification> userNotifications = userRepository.findAll().stream()
        .filter(user -> user.getRole() != null && AppConstant.ROLE_USER.equals(user.getRole().getName()))
        .map(user -> {
          Notification userNotification = new Notification();
          userNotification.setUser(user);
          userNotification.setTitle(notification.getTitle());
          userNotification.setContent(notification.getContent());
          userNotification.setType(notification.getType());
          userNotification.setReferenceIds(referenceIds);
          userNotification.setNotificationDate(Instant.now());
          userNotification.setRead(false);
          userNotification.setStartPromotionDate(notification.getStartPromotionDate());
          userNotification.setEndPromotionDate(notification.getEndPromotionDate());
          return userNotification;
        }).collect(Collectors.toList());

    notificationRepository.saveAll(userNotifications);
    log.debug("Created notifications for {} users with USER role", userNotifications.size());
  }

  @Override
  @Transactional
  public void processScheduledNotifications() {
    Instant now = Instant.now();
    List<ScheduledNotification> pendingNotifications =
        scheduledNotificationRepository.findPendingScheduledNotifications(now);

    for (ScheduledNotification scheduledNotification : pendingNotifications) {
      if (scheduledNotification.getType() == NotificationType.PROMOTION_NOTIFICATION) {
        // Find the promotion
        promotionRepository.findById(Long.parseLong(scheduledNotification.getReferenceIds()))
            .ifPresent(promotion -> {
              // Get product IDs (if available)
              HashSet<Long> productIds =
                  new HashSet<>(promotion.getProducts().stream().map(Product::getId).toList());
              promotion.getCategories().forEach(category -> productIds
                  .addAll(category.getProducts().stream().map(Product::getId).toList()));

              // Create DTO
              NotificationResDTO.PromotionNotificationDTO notificationDTO =
                  createPromotionNotificationDTO(scheduledNotification, promotion);

              // Create notifications for all users
              createNotificationsForAllUsers(notificationDTO, productIds);

              // Send broadcast
              sendBroadcastNotification(notificationDTO);

              // Mark as sent
              scheduledNotification.setSent(true);
              scheduledNotificationRepository.save(scheduledNotification);
            });
      }
    }
  }

  // Helper methods
  private NotificationResDTO convertToNotificationDTO(Notification notification) {
    return NotificationResDTO.builder().id(notification.getId()).title(notification.getTitle())
        .content(notification.getContent()).type(notification.getType()).read(notification.isRead())
        .notificationDate(
            notification.getNotificationDate())
        .referenceIds(notification.getReferenceIds())
        .startPromotionDate(
            notification.getStartPromotionDate() != null ? notification.getStartPromotionDate()
                : null)
        .endPromotionDate(
            notification.getEndPromotionDate() != null ? notification.getEndPromotionDate() : null)
        .build();
  }

  private NotificationResDTO.PromotionNotificationDTO createPromotionNotificationDTO(
      Object notification, Promotion promotion) {
    // Build the notification DTO
    NotificationResDTO.PromotionNotificationDTO.PromotionNotificationDTOBuilder builder =
        NotificationResDTO.PromotionNotificationDTO.builder();

    if (notification instanceof ScheduledNotification) {
      ScheduledNotification notif = (ScheduledNotification) notification;
      builder.id(notif.getId()).title(notif.getTitle()).content(notif.getContent())
          .type(notif.getType()).referenceIds(notif.getReferenceIds())
          .startPromotionDate(promotion.getStartDate()).endPromotionDate(promotion.getEndDate());
    } else {
      // For newly created notifications without saved entity yet
      builder.title(promotion.getName()).content(promotion.getDescription())
          .type(NotificationType.PROMOTION_NOTIFICATION).referenceIds(promotion.getId().toString())
          .startPromotionDate(promotion.getStartDate()).endPromotionDate(promotion.getEndDate());
    }

    return builder.build();
  }

  @Override
  public void markAllNotificationAsRead() {
    User currentUser = securityUtil.getCurrentUser();
    List<Notification> unreadNotifications =
        notificationRepository.findByUserAndReadFalse(currentUser);

    unreadNotifications.forEach(notification -> notification.setRead(true));
    notificationRepository.saveAll(unreadNotifications);

    log.debug("Marked {} notifications as read for user {}", unreadNotifications.size(),
        currentUser.getEmail());
  }

  @Async
  @Override
  public void sendNotificationToUser(Long userId, String title, String body,
      Map<String, String> data) {
    // 1. Lấy tất cả các device token của người dùng
    List<String> tokens = userDeviceRepository.findByUserId(userId).stream()
        .map(UserDevice::getDeviceToken).collect(Collectors.toList());

    if (tokens.isEmpty()) {
      log.debug("Không tìm thấy thiết bị nào cho người dùng: {}", userId);
      return;
    }

    // 2. Tạo thông báo
    // Sử dụng MulticastMessage để gửi đến nhiều thiết bị cùng lúc
    MulticastMessage message =
        MulticastMessage
            .builder().setNotification(com.google.firebase.messaging.Notification.builder()
                .setTitle(title).setBody(body).build())
            .putAllData(data).addAllTokens(tokens).build();
    try {
      // 3. Gửi thông báo
      BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
      log.debug("{} messages were sent successfully", response.getSuccessCount());

      // 4. Xử lý các token không hợp lệ (ví dụ: người dùng đã gỡ app)
      if (response.getFailureCount() > 0) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
          if (!responses.get(i).isSuccessful()) {
            String failedToken = tokens.get(i);
            String errorCode = responses.get(i).getException().getMessagingErrorCode().name();
            // Nếu lỗi là UNREGISTERED, nghĩa là token không còn hợp lệ -> xóa khỏi DB
            if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
              log.debug("Xóa token không hợp lệ: {}", failedToken);
              userDeviceRepository.findByDeviceToken(failedToken)
                  .ifPresent(userDevice -> userDeviceRepository.delete(userDevice));
            }
          }
        }
      }
    } catch (FirebaseMessagingException e) {
      log.error("Lỗi khi gửi thông báo: ", e);
    }
  }

  private String getStatusDisplay(OrderStatus status) {
    switch (status) {
      case PENDING:
        return "Chờ xử lý";
      case PROCESSING:
        return "Đang xử lý";
      case SHIPPING:
        return "Đang giao hàng";
      case DELIVERED:
        return "Đã giao hàng";
      case CANCELLED:
        return "Đã hủy";
      case RETURNED:
        return "Đã hoàn trả";
      default:
        return status.toString();
    }
  }
}
