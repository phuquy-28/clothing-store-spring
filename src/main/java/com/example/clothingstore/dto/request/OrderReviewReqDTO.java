package com.example.clothingstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class OrderReviewReqDTO {

  @NotNull(message = "orderId.not.null")
  private Long orderId;

  @NotEmpty(message = "reviewItems.not.empty")
  private List<ReviewItem> reviewItems;

  @Data
  @Valid
  public static class ReviewItem {
    @NotNull(message = "lineItemId.not.null")
    private Long lineItemId;

    @NotNull(message = "rating.not.null")
    @Min(value = 1, message = "rating.min")
    @Max(value = 5, message = "rating.max")
    private Integer rating;

    private String description;
  }
}
