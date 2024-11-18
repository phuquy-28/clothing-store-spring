package com.example.clothingstore.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionResDTO {
  
  private Long id;

  private String name;

  private Double discountRate;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  private String description;

  private List<ProductResDTO> products;

  private List<CategoryResDTO> categories;
}
