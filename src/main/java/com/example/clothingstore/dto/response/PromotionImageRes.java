package com.example.clothingstore.dto.response;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionImageRes {

  private Long id;
  private String name;
  private String description;
  private Double discountRate;
  private Instant startDate;
  private Instant endDate;
  private String imageUrl;
}
