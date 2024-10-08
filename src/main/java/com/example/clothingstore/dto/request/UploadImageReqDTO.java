package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadImageReqDTO {

  @NotBlank(message = "file.name.not.blank")
  private String fileName;
}
