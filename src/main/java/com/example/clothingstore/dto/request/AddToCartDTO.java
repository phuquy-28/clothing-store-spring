package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddToCartDTO {

  @NotNull(message = "product.variant.id.not.null")
  private Long productVariantId;

  @NotNull(message = "quantity.not.null")
  @Min(value = 1, message = "quantity.min")
  private Integer quantity;
}
