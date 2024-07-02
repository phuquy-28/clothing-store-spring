package com.example.clothingstore.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqLoginDTO {
    @NotBlank(message = "Username cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
