package com.example.clothingstore.dto.request;

import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.PointActionType;
import com.google.auto.value.AutoValue.Builder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Builder
public class PointHistoryReqDTO {
  
  @NotNull(message = "points.not.null")
  @Min(value = 1, message = "points.min")
  private Long points;

  @EnumValue(enumClass = PointActionType.class, message = "action.type.invalid")
  private String actionType;

  @NotBlank(message = "description.not.blank")
  private String description;

  @NotBlank(message = "email.not.blank")
  private String emailUser;
}
