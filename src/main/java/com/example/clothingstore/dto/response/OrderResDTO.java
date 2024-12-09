package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.enumeration.Size;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
@Data
@Builder
public class OrderResDTO {

  private Long id;
  
  private String code;

  private Instant orderDate;

  private OrderStatus status;

  private PaymentMethod paymentMethod;

  private PaymentStatus paymentStatus;

  private List<LineItem> lineItems;

  private Double total;

  private Double shippingFee;

  private Double discount;

  private Double finalTotal;

  private Boolean canReview;

  private Boolean isReviewed;

  private String cancelReason;

  @Data
  @Builder
  public static class LineItem {

    private Long id;

    private String productName;

    private Color color;

    private Size size;

    private String variantImage;

    private Long quantity;

    private Double unitPrice;

    private Double discount;
  }
}
