package com.example.clothingstore.entity;

import com.example.clothingstore.enumeration.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "scheduled_notifications",
    indexes = {@Index(name = "idx_scheduled_notification_type", columnList = "type"),
        @Index(name = "idx_scheduled_notification_sent", columnList = "is_sent"),
        @Index(name = "idx_scheduled_notification_date", columnList = "scheduled_date")})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledNotification extends AbstractEntity {

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private NotificationType type;

  @Column(name = "reference_ids")
  private String referenceIds;

  @Column(name = "scheduled_date", nullable = false)
  private Instant scheduledDate;

  @Column(name = "is_sent", nullable = false)
  private boolean sent = false;

  private Instant startPromotionDate;

  private Instant endPromotionDate;
}
