package com.example.clothingstore.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqRegisterDTO {

  @NotBlank(message = "Email không được để trống")
  private String email;

  @NotBlank(message = "Mật khẩu không được để trống")
  private String password;

  @NotBlank(message = "First name không được để trống")
  private String firstName;

  @NotBlank(message = "Last name không được để trống")
  private String lastName;
}
