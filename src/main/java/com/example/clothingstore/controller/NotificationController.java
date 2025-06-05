package com.example.clothingstore.controller;

import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.FCMTokenReq;
import com.example.clothingstore.dto.request.NotificationReqDTO;
import com.example.clothingstore.dto.response.NotificationResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.service.NotificationService;
import com.example.clothingstore.service.UserDeviceService;
import com.example.clothingstore.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class NotificationController {

  private final Logger log = LoggerFactory.getLogger(NotificationController.class);
  private final NotificationService notificationService;
  private final UserDeviceService userDeviceService;

  @GetMapping(UrlConfig.NOTIFICATION)
  public ResponseEntity<ResultPaginationDTO> getUserNotifications(Pageable pageable) {
    log.debug("REST request to get notifications for current user");
    return ResponseEntity.ok(notificationService.getUserNotifications(pageable));
  }

  @GetMapping(UrlConfig.NOTIFICATION + UrlConfig.UNREAD_COUNT)
  public ResponseEntity<Long> getUnreadNotificationCount() {
    log.debug("REST request to get unread notification count for current user");
    return ResponseEntity.ok(notificationService.getUnreadNotificationCount());
  }

  @PutMapping(UrlConfig.NOTIFICATION + UrlConfig.MARK_READ + UrlConfig.ID)
  public ResponseEntity<NotificationResDTO> markNotificationAsRead(
      @PathVariable("id") Long notificationId) {
    log.debug("REST request to mark notification as read: {}", notificationId);
    return ResponseEntity.ok(notificationService.markNotificationAsRead(notificationId));
  }

  @PostMapping(UrlConfig.NOTIFICATION + UrlConfig.MARK_READ_ALL)
  public ResponseEntity<Void> markAllNotificationAsRead() {
    log.debug("REST request to mark all notifications as read");
    notificationService.markAllNotificationAsRead();
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.PROMOTION_NOTIFICATION)
  public ResponseEntity<NotificationResDTO.PromotionNotificationDTO> createPromotionNotification(
      @Valid @RequestBody NotificationReqDTO.CreatePromotionNotificationDTO dto) {
    log.debug("REST request to create promotion notification: {}", dto);
    return ResponseEntity.ok(notificationService.createPromotionNotification(dto));
  }

  @PostMapping(UrlConfig.NOTIFICATION + UrlConfig.SAVE_FCM_TOKEN)
  public ResponseEntity<Void> saveFCMToken(@Valid @RequestBody FCMTokenReq fcmTokenReq) {
    String email = SecurityUtil.getCurrentUserLogin().get();
    log.debug("REST request to save FCM token: {} for user: {}", fcmTokenReq, email);
    userDeviceService.saveFCMToken(fcmTokenReq.getDeviceToken(), email);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping(UrlConfig.NOTIFICATION + UrlConfig.DELETE_FCM_TOKEN)
  public ResponseEntity<Void> deleteFCMToken(@Valid @RequestBody FCMTokenReq fcmTokenReq) {
    String email = SecurityUtil.getCurrentUserLogin().get();
    log.debug("REST request to delete FCM token: {} for user: {}", fcmTokenReq, email);
    userDeviceService.deleteFCMToken(fcmTokenReq.getDeviceToken(), email);
    return ResponseEntity.ok().build();
  }

}
