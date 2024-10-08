package com.example.clothingstore.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResDTO {

  private Long id;

  private String name;

  private String description;

  private Double price;

  private String slug;

  private Double discountRate;

  private Long categoryId;

  private List<ProductImageResDTO> images;

  private List<ProductVariantResDTO> variants;

  @Data
  @Builder
  public static class ProductImageResDTO {
    private Long id;
    private String url;
  }

  @Data
  @Builder
  public static class ProductVariantResDTO {
    private Long id;
    private String color;
    private String size;
    private Integer quantity;
  }
}
