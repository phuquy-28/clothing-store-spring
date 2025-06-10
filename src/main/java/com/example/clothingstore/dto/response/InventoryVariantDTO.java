package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryVariantDTO {
  private Long variantId;
  private String sku;
  private Long productId;
  private String productName;
  private String variantImage;
  private String color;
  private String size;
  private Integer quantityInStock;
  private Long quantitySold;
}
