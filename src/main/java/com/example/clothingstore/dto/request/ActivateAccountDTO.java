package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivateAccountDTO {

  @NotBlank(message = "email.not.blank")
  @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "email.not.valid")
  private String email;

  @NotBlank(message = "activation.code.not.blank")
  @Size(min = 6, max = 6, message = "activation.code.size.not.valid")
  private String activationCode;
}
