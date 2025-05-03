package com.example.clothingstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import com.example.clothingstore.enumeration.OrderStatus;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @Enumerated(EnumType.STRING)
  private OrderStatus previousStatus;

  @Enumerated(EnumType.STRING)
  private OrderStatus newStatus;

  private Instant updateTimestamp;

  private String updatedBy;

  private String note;
}
