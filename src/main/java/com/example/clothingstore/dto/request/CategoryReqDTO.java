package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryReqDTO {

  private Long id;

  @NotBlank(message = "category.name.not.blank")
  private String name;

  @NotBlank(message = "category.image.not.blank")
  private String imageUrl;
}
