package com.example.clothingstore.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutReqDTO {
  
  @NotBlank(message = "refresh.token.required")
  @JsonProperty("refresh_token")
  private String refreshToken;
}
