package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlySpendingChartRes {
  private List<String> labels; // e.g. ["7/23", "8/23", "9/23"]

  private List<Double> values; // corresponding amounts for each month

  private List<Integer> counts; // order counts for each month
}
