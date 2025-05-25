package com.example.clothingstore.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogoutReqDTO {
  
  @JsonProperty("refresh_token")
  private String refreshToken;
}
