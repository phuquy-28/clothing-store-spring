package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.NotificationReqDTO;
import com.example.clothingstore.dto.response.NotificationResDTO;
import com.example.clothingstore.entity.Notification;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.entity.ScheduledNotification;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.NotificationType;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.NotificationRepository;
import com.example.clothingstore.repository.PromotionRepository;
import com.example.clothingstore.repository.ScheduledNotificationRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.NotificationService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final NotificationRepository notificationRepository;
  private final ScheduledNotificationRepository scheduledNotificationRepository;
  private final PromotionRepository promotionRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final SecurityUtil securityUtil;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void createOrderStatusNotification(Order order) {
    User user = order.getUser();

    // Create notification content based on order status
    String title = "Order Status Updated";
    String content = String.format("Your order #%s has been updated to: %s", order.getCode(),
        order.getStatus().toString());

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

    // Send notification via WebSocket
    sendNotificationToUser(user, notificationDTO);
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
  public NotificationResDTO.NotificationListDTO getUserNotifications(Pageable pageable) {
    User currentUser = securityUtil.getCurrentUser();
    List<Notification> notificationsPage =
        notificationRepository.findByUserOrderByNotificationDateDesc(currentUser);

    List<NotificationResDTO> notifications =
        notificationsPage.stream().map(this::convertToNotificationDTO).collect(Collectors.toList());

    long unreadCount = notificationRepository.countByUserAndReadFalse(currentUser);

    return NotificationResDTO.NotificationListDTO.builder().notifications(notifications)
        .unreadCount(unreadCount).build();
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

    List<Notification> userNotifications = userRepository.findAll().stream().map(user -> {
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
    log.debug("Created notifications for {} users", userNotifications.size());
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
            LocalDateTime.ofInstant(notification.getNotificationDate(), ZoneId.systemDefault()))
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
}
