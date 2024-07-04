package com.example.clothingstore.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqRegisterDTO {

  @NotBlank(message = "Email cannot be blank")
  private String email;

  @NotBlank(message = "Password cannot be blank")
  private String password;

  @NotBlank(message = "First name cannot be blank")
  private String firstName;

  @NotBlank(message = "Last name cannot be blank")
  private String lastName;
}
