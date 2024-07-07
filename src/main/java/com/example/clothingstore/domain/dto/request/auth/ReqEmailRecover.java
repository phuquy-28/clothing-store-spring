package com.example.clothingstore.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqEmailRecover {
    @NotBlank(message = "Email không được để trống")
    private String email;
}
