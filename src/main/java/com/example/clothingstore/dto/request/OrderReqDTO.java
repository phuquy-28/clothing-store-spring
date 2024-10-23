package com.example.clothingstore.dto.request;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderReqDTO {

  @Valid
  @NotNull(message = "shippingProfile.not.null")
  private ShippingProfileReqDTO shippingProfile;

  @Valid
  @NotEmpty(message = "lineItems.not.empty")
  private List<LineItemReqDTO> lineItems;

  private String note;

  @Data
  public static class LineItemReqDTO {

    @NotNull(message = "lineItem.productVariantId.not.null")
    private Long productVariantId;

    @NotNull(message = "lineItem.quantity.not.null")
    @Min(value = 0, message = "lineItem.quantity.min")
    private Integer quantity;
  }
}
