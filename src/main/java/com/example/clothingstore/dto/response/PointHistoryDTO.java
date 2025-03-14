package com.example.clothingstore.dto.response;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointHistoryDTO {

  private Long id;

  private Long points;

  private String actionType;

  private String description;

  private String orderCode;

  private Instant createdAt;
}
