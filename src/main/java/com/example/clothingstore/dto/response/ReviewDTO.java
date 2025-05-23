package com.example.clothingstore.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import java.util.List;

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
}
