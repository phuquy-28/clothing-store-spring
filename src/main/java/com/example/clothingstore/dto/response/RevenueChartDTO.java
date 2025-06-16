package com.example.clothingstore.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueChartDTO {
  private List<String> labels;
  private List<DatasetDTO> datasets;

  @Data
  @Builder
  public static class DatasetDTO {
    private String label;
    private List<? extends Number> data;
  }
}
