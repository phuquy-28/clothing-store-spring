package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqResetPassword {
  @NotBlank(message = "password.not.blank")
  private String newPassword;

  @NotBlank(message = "confirm.password.not.blank")
  private String confirmPassword;
}
