package com.example.clothingstore.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.Size;

@Data
@Builder
public class ReviewDTO {

  private Long reviewId;

  private String description;

  private Double rating;

  private List<String> imageUrls;

  private String videoUrl;

  private LocalDateTime createdAt;

  private boolean isPublished;

  private UserReviewDTO userReviewDTO;

  private ProductVariantDTO productVariantDTO;

  private String orderCode;

  @Data
  @Builder
  public static class UserReviewDTO {
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private Double totalSpend;

    private Long totalReview;
  }

  @Data
  @Builder
  public static class ProductVariantDTO {
    private Long id;
    
    private String productName;
    
    private Color color;
    
    private Size size;
    
    private Double price;

    private String imageUrl;
  }
}
