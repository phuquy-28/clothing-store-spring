package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
public class DashboardSummaryDTO {
  private MetricDTO totalSales;
  private MetricDTO visitors;
  private MetricDTO totalOrders;
  private MetricDTO refunded;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class MetricDTO {
    private Double value;
    private Double changePercentage;
  }
}
