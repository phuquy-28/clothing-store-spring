package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.NotificationType;
import lombok.Builder;
import lombok.Data;

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
  private Long referenceId;
  private String imageUrl;

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
    private Long referenceId;
    private String imageUrl;
    private List<ProductResDTO> promotionProducts;
  }
}
