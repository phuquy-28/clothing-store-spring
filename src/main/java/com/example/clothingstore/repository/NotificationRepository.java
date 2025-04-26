package com.example.clothingstore.repository;

import com.example.clothingstore.entity.Notification;
import com.example.clothingstore.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
  List<Notification> findByUserOrderByNotificationDateDesc(User user);
  
  long countByUserAndReadFalse(User user);
} 