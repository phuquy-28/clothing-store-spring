package com.example.clothingstore.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromotionReqDTO {
  
  private Long id;

  @NotBlank(message = "promotion.name.not.blank")
  private String name;

  @NotNull(message = "promotion.discountRate.not.null")
  @Min(value = 0, message = "promotion.discountRate.min")
  @Max(value = 100, message = "promotion.discountRate.max")
  private Double discountRate;

  @NotBlank(message = "promotion.description.not.blank")
  private String description;

  @NotNull(message = "promotion.startDate.not.null")
  private LocalDateTime startDate;

  @NotNull(message = "promotion.endDate.not.null")
  private LocalDateTime endDate;

  private List<Long> productIds;

  private List<Long> categoryIds;

  private String imageUrl;
}
