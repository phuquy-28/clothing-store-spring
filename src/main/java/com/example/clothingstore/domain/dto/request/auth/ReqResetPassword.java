package com.example.clothingstore.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqResetPassword {
  @NotBlank(message = "Mật khẩu mới không được để trống")
  private String newPassword;

  @NotBlank(message = "Xác nhận mật khẩu không được để trống")
  private String confirmPassword;
}
