package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.NotificationReqDTO;
import com.example.clothingstore.dto.response.NotificationResDTO;
import com.example.clothingstore.dto.response.ProductResDTO;
import com.example.clothingstore.entity.Notification;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Promotion;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.NotificationType;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.NotificationRepository;
import com.example.clothingstore.repository.PromotionRepository;
import com.example.clothingstore.service.NotificationService;
import com.example.clothingstore.service.ProductService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final NotificationRepository notificationRepository;
  private final PromotionRepository promotionRepository;
  private final ProductService productService;
  private final SimpMessagingTemplate messagingTemplate;
  private final SecurityUtil securityUtil;

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
    notification.setReferenceId(order.getId());
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

    // Create broadcast notification
    Notification notification = new Notification();
    notification.setTitle(dto.getTitle());
    notification.setContent(dto.getContent());
    notification.setType(NotificationType.PROMOTION_NOTIFICATION);
    notification.setReferenceId(promotion.getId());
    notification.setImageUrl(dto.getImageUrl());
    notification.setBroadcast(true);
    notification.setNotificationDate(Instant.now());

    // Set scheduled date if provided
    if (dto.getScheduledDate() != null) {
      notification.setScheduledDate(dto.getScheduledDate().toInstant(ZoneOffset.UTC));
      notification.setSent(false);
    } else {
      notification.setSent(true);
    }

    notification = notificationRepository.save(notification);

    // Create DTO with promotion products
    NotificationResDTO.PromotionNotificationDTO notificationDTO =
        createPromotionNotificationDTO(notification, promotion);

    // If not scheduled, broadcast immediately
    if (dto.getScheduledDate() == null) {
      sendBroadcastNotification(notificationDTO);
    }

    return notificationDTO;
  }

  @Override
  public NotificationResDTO.NotificationListDTO getUserNotifications(Pageable pageable) {
    User currentUser = securityUtil.getCurrentUser();
    Page<Notification> notificationsPage =
        notificationRepository.findByUserOrderByNotificationDateDesc(currentUser, pageable);

    List<NotificationResDTO> notifications = notificationsPage.getContent().stream()
        .map(this::convertToNotificationDTO).collect(Collectors.toList());

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
  public void processScheduledNotifications() {
    Instant now = Instant.now();
    List<Notification> pendingNotifications =
        notificationRepository.findPendingBroadcastNotifications(now);

    for (Notification notification : pendingNotifications) {
      if (notification.getType() == NotificationType.PROMOTION_NOTIFICATION) {
        // Find the promotion
        promotionRepository.findById(notification.getReferenceId()).ifPresent(promotion -> {
          // Create DTO
          NotificationResDTO.PromotionNotificationDTO notificationDTO =
              createPromotionNotificationDTO(notification, promotion);

          // Send broadcast
          sendBroadcastNotification(notificationDTO);

          // Mark as sent
          notification.setSent(true);
          notificationRepository.save(notification);
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
        .referenceId(notification.getReferenceId()).imageUrl(notification.getImageUrl()).build();
  }

  private NotificationResDTO.PromotionNotificationDTO createPromotionNotificationDTO(
      Notification notification, Promotion promotion) {
    List<ProductResDTO> promotionProducts = promotion.getProducts().stream()
        .map(productService::convertToProductResDTO).collect(Collectors.toList());

    return NotificationResDTO.PromotionNotificationDTO.builder().id(notification.getId())
        .title(notification.getTitle()).content(notification.getContent())
        .type(notification.getType()).referenceId(notification.getReferenceId())
        .imageUrl(notification.getImageUrl()).promotionProducts(promotionProducts).build();
  }
}
