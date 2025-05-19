package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthCodeRequestDTO {
  @NotBlank(message = "code.not.blank")
  private String code;
}
