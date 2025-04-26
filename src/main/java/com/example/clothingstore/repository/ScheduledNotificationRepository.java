package com.example.clothingstore.repository;

import com.example.clothingstore.entity.ScheduledNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ScheduledNotificationRepository
    extends JpaRepository<ScheduledNotification, Long> {

  @Query("SELECT sn FROM ScheduledNotification sn WHERE sn.scheduledDate <= :now AND sn.sent = false")
  List<ScheduledNotification> findPendingScheduledNotifications(@Param("now") Instant now);
}
