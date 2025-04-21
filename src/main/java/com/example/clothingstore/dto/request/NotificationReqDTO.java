package com.example.clothingstore.dto.request;

import com.example.clothingstore.enumeration.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationReqDTO {
  private Long id;

  @NotBlank(message = "notification.title.not.blank")
  private String title;

  @NotBlank(message = "notification.content.not.blank")
  private String content;

  @NotNull(message = "notification.type.not.null")
  private NotificationType type;

  private Long referenceId;

  private String imageUrl;

  private boolean broadcast;

  private LocalDateTime scheduledDate;

  @Data
  public static class MarkAsReadDTO {
    private Long notificationId;
  }

  @Data
  public static class CreatePromotionNotificationDTO {
    @NotBlank(message = "notification.title.not.blank")
    private String title;

    @NotBlank(message = "notification.content.not.blank")
    private String content;

    @NotNull(message = "notification.promotionId.not.null")
    private Long promotionId;

    private String imageUrl;

    private LocalDateTime scheduledDate;
  }
}
