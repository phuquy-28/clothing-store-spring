package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReturnRequestReqDTO {

  @NotNull(message = "orderId.not.null")
  private Long orderId;

  @NotBlank(message = "reason.not.blank")
  private String reason;

  // Bank information for COD orders refund
  private String bankName;

  private String accountNumber;

  private String accountHolderName;

  @Size(max = 3, message = "return.images.max.size")
  private String[] imageUrls;
}
