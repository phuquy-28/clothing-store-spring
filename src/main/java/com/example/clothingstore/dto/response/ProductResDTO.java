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

  private Long categoryId;

  private List<String> images;

  private List<ProductVariantResDTO> variants;

  @Data
  @Builder
  public static class ProductVariantResDTO {
    private Long id;
    private String color;
    private String size;
    private Integer quantity;
    private Double differencePrice;
    private List<String> images;
  }
}