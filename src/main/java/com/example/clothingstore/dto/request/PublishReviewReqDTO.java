package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PublishReviewReqDTO {
  @NotNull(message = "review.id.not.null")
  private Long reviewId;

  @NotNull(message = "review.published.not.null")
  private Boolean published;
}
