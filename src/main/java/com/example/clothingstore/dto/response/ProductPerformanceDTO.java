package com.example.clothingstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPerformanceDTO {
  private Long productId;
  private String productName;
  private String imageUrl;
  private Long quantitySold;
  private Double totalSales;
}
