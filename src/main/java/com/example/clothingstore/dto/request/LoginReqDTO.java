package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginReqDTO {

  @NotBlank(message = "{email.not.blank}")
  @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "{email.not.valid}")
  private String email;

  @NotBlank(message = "{password.not.blank}")
  private String password;
}
