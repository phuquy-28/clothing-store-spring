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

  private Double minPrice;

  private Double maxPrice;

  private Double priceWithDiscount;

  private Double minPriceWithDiscount;

  private Double maxPriceWithDiscount;

  private Long categoryId;

  private String categoryName;

  private boolean isFeatured;

  private Double discountRate;

  private Double averageRating;

  private Long numberOfReviews;

  private String slug;

  private String colorDefault;

  private List<String> images;

  private List<ProductVariantResDTO> variants;

  @Data
  @Builder
  public static class ProductVariantResDTO {
    private Long id;
    private String color;
    private String size;
    private Integer quantity;
    private Long currentUserCartQuantity;
    private Double differencePrice;
    private List<String> images;
  }
}
