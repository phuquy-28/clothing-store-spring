package com.example.clothingstore.scheduler;

import com.example.clothingstore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

  private final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
  private final NotificationService notificationService;

  /**
   * Process scheduled notifications every hour. This scheduler checks for any scheduled
   * notifications that need to be sent and creates user notifications for them.
   */
  @Scheduled(cron = "0 0 * * * ?") // Run every hour
  public void processScheduledNotifications() {
    log.debug("Running scheduled task to process pending notifications");
    notificationService.processScheduledNotifications();
  }
}
