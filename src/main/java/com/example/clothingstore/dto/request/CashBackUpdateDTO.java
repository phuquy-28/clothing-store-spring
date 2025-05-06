package com.example.clothingstore.dto.request;

import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.CashBackStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CashBackUpdateDTO {

  @NotNull(message = "return.request.id.not.null")
  private Long returnRequestId;

  @EnumValue(enumClass = CashBackStatus.class, message = "cashback.status.invalid")
  private String cashBackStatus;
}
