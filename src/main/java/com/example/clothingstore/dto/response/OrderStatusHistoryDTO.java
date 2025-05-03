package com.example.clothingstore.dto.response;

import java.time.Instant;
import com.example.clothingstore.enumeration.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusHistoryDTO {
  private Long id;

  private Long orderId;

  private OrderStatus previousStatus;

  private OrderStatus newStatus;

  private Instant updateTimestamp;

  private String updatedBy;

  private String note;
}
