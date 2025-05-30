package com.example.clothingstore.repository;

import com.example.clothingstore.entity.Notification;
import com.example.clothingstore.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByUserOrderByNotificationDateDesc(User user, Pageable pageable);

  long countByUserAndReadFalse(User user);

  List<Notification> findByUserAndReadFalse(User user);

}
