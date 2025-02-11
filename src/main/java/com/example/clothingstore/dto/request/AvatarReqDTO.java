package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarReqDTO {

  @NotBlank(message = "avatar.not.blank")
  private String avatar;
}
