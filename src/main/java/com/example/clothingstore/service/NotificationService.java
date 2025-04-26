package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.NotificationReqDTO;
import com.example.clothingstore.dto.response.NotificationResDTO;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.User;
import java.util.HashSet;
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

  void markAllNotificationAsRead();

  // Send notifications via WebSocket
  void sendNotificationToUser(User user, NotificationResDTO notification);

  void sendBroadcastNotification(NotificationResDTO.PromotionNotificationDTO notification);

  // Create notifications for all users from a broadcast message
  void createNotificationsForAllUsers(NotificationResDTO.PromotionNotificationDTO notification,
      HashSet<Long> referenceIds);

  // Process scheduled notifications
  void processScheduledNotifications();
}
