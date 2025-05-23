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
@Table(name = "notifications",
    indexes = {@Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_type", columnList = "type"),
        @Index(name = "idx_notification_read", columnList = "is_read")})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private NotificationType type;

  @Column(name = "is_read", nullable = false)
  private boolean read = false;

  @Column(nullable = false)
  private Instant notificationDate;

  @Column(name = "reference_ids")
  private String referenceIds;

  private Instant startPromotionDate;

  private Instant endPromotionDate;
}
