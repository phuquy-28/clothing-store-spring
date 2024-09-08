package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterReqDTO {

  @NotBlank(message = "{email.not.blank}")
  @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "{email.not.valid}")
  private String email;

  @NotBlank(message = "{password.not.blank}")
  @Size(min = 6, message = "{password.not.valid}")
  private String password;

  @NotBlank(message = "{first.name.not.blank}")
  private String firstName;

  @NotBlank(message = "{last.name.not.blank}")
  private String lastName;
}
