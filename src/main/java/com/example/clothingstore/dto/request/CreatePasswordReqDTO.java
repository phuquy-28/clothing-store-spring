package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePasswordReqDTO {

  @NotBlank(message = "password.not.blank")
  @Size(min = 6, message = "password.not.valid")
  private String password;
  
  @NotBlank(message = "confirm.password.not.blank")
  private String confirmPassword;
} 