package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqEmailRecover {
    @NotBlank(message = "email.not.blank")
    private String email;
}
