package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutReqDTO {
  
  @NotBlank(message = "refresh.token.required")
  private String refreshToken;
}
