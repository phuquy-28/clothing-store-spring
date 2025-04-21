package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusSpendingChartRes {
  private Long pending;

  private Long processing;

  private Long shipping;

  private Long delivered;
}
