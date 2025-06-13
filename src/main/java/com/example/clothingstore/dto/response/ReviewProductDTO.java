package com.example.clothingstore.dto.response;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Builder
@Data
public class ReviewProductDTO {

  private Long reviewId;

  private String firstName;

  private String lastName;

  private String avatar;

  private Double rating;

  private Instant createdAt;

  private BoughtVariantDTO variant;

  private String description;

  private List<String> imageUrls;

  private String videoUrl;

  @Builder
  @Data
  public static class BoughtVariantDTO {
    private Long variantId;

    private String color;

    private String size;
  }
}
