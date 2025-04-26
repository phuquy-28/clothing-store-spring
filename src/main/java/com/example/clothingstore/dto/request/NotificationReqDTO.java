package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationReqDTO {

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

    private LocalDateTime scheduledDate;
  }
}
