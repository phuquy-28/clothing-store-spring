package com.example.clothingstore.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderPreviewDTO {

  private ShippingProfileResDTO shippingProfile;

  private List<CartItemDTO> lineItems;

  private Double finalTotal;

  private Double shippingFee;

  private Instant estimatedDeliveryDate;

  private Double discount;

  private Double pointDiscount;

  private Long points;
}
