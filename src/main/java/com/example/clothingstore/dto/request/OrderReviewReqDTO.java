package com.example.clothingstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class OrderReviewReqDTO {

  @NotNull(message = "orderId.not.null")
  private Long orderId;

  @NotNull(message = "reviewItem.not.null")
  private ReviewItem reviewItem;

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
    
    @Size(max = 5, message = "imageUrls.max.size")
    private List<String> imageUrls;
    
    private String videoUrl;
  }
}
