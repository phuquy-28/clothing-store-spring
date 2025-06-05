package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FCMTokenReq {

  @NotBlank(message = "device.token.required")
  private String deviceToken;

}
