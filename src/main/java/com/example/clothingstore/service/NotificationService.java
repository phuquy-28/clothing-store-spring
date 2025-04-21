package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.NotificationReqDTO;
import com.example.clothingstore.dto.response.NotificationResDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.User;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

  // Create notifications
  void createOrderStatusNotification(Order order);

  NotificationResDTO.PromotionNotificationDTO createPromotionNotification(
      NotificationReqDTO.CreatePromotionNotificationDTO createPromotionNotificationDTO);

  // Get notifications
  NotificationResDTO.NotificationListDTO getUserNotifications(Pageable pageable);

  long getUnreadNotificationCount();

  // Mark notifications as read
  NotificationResDTO markNotificationAsRead(Long notificationId);

  // Send notifications via WebSocket
  void sendNotificationToUser(User user, NotificationResDTO notification);

  void sendBroadcastNotification(NotificationResDTO.PromotionNotificationDTO notification);

  // Process scheduled notifications
  void processScheduledNotifications();
}
