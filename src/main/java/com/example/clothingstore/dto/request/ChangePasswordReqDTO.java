package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordReqDTO {

  @NotBlank(message = "old.password.not.blank")
  private String oldPassword;

  @NotBlank(message = "new.password.not.blank")
  @Size(min = 6, message = "password.not.valid")
  private String newPassword;

  @NotBlank(message = "confirm.password.not.blank")
  private String confirmPassword;
}
