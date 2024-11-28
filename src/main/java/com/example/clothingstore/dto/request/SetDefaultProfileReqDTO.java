package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetDefaultProfileReqDTO {

  @NotNull(message = "shipping.profile.id.not.null")
  private Long shippingProfileId;
}
