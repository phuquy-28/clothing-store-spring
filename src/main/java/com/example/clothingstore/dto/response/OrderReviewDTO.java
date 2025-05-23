package com.example.clothingstore.dto.response;

import java.time.Instant;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.Size;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderReviewDTO {

  private Long lineItemId;

  private String productName;

  private Color color;

  private Size size;

  private String variantImage;

  private String firstName;

  private String lastName;

  private String avatar;

  private Instant createdAt;

  private Double rating;

  private String description;

  private List<String> imageUrls;

  private String videoUrl;
}
