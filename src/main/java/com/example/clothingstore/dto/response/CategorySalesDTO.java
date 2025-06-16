package com.example.clothingstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySalesDTO {
  private String categoryName;
  private Double totalSales;
}
