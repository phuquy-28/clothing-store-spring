package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetAccountDTO {
  @NotBlank(message = "email.not.blank")
  @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "email.not.valid")
  private String email;

  @NotBlank(message = "reset.code.not.blank")
  @Size(min = 6, max = 6, message = "reset.code.size.not.valid")
  private String resetCode;

  @NotBlank(message = "password.not.blank")
  private String newPassword;

  @NotBlank(message = "confirm.password.not.blank")
  private String confirmPassword;
}
