package com.example.clothingstore.repository;

import com.example.clothingstore.entity.Notification;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
  Page<Notification> findByUserOrderByNotificationDateDesc(User user, Pageable pageable);
  
  List<Notification> findByUserAndReadFalseOrderByNotificationDateDesc(User user);
  
  long countByUserAndReadFalse(User user);
  
  @Query("SELECT n FROM Notification n WHERE n.scheduledDate <= :now AND n.sent = false AND n.broadcast = true")
  List<Notification> findPendingBroadcastNotifications(@Param("now") Instant now);
  
  @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.referenceId = :referenceId AND n.user = :user")
  List<Notification> findByTypeAndReferenceIdAndUser(
          @Param("type") NotificationType type, 
          @Param("referenceId") Long referenceId,
          @Param("user") User user);
  
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.read = false")
  long countUnreadNotifications(@Param("user") User user);
} 