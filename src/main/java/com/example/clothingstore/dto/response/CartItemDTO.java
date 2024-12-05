package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDTO {
  private Long cartItemId;

  private Long productId;

  private String slug;

  private String productName;

  private ProductVariantDTO productVariant;

  private Double price;

  private Double finalPrice;

  private Integer quantity;

  private Integer inStock;

  private String image;

  @Data
  @Builder
  public static class ProductVariantDTO {
    private Long id;
    private String color;
    private String size;
    private String image;
  }
}
