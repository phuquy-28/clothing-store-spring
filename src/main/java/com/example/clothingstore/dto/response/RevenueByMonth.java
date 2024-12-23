package com.example.clothingstore.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueByMonth {
  
  private List<RevenueByMonthDTO> revenueByMonth;

  @Data
  @Builder
  public static class RevenueByMonthDTO {
    private int month;
    private double revenue;
  }
}
