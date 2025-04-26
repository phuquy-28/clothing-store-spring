package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NotificationResDTO {
  private Long id;
  private String title;
  private String content;
  private NotificationType type;
  private boolean read;
  private LocalDateTime notificationDate;
  private String referenceIds;
  private Instant startPromotionDate;
  private Instant endPromotionDate;

  @Data
  @Builder
  public static class NotificationListDTO {
    private List<NotificationResDTO> notifications;
    private long unreadCount;
  }

  @Data
  @Builder
  public static class PromotionNotificationDTO {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private String referenceIds;
    private Instant startPromotionDate;
    private Instant endPromotionDate;
  }
}
