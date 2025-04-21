package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatisticsSummaryRes {
  private double totalAmount;
  private int totalOrderCount;

  private StatusBreakdown statusBreakdown;

  @Data
  @Builder
  public static class StatusBreakdown {
    private Status pending;
    private Status processing;
    private Status shipping;
    private Status delivered;
  }

  @Data
  @Builder
  public static class Status {
    private int count;
    private double amount;
  }
}
