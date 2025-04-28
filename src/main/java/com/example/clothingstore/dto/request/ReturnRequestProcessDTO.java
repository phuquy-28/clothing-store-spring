package com.example.clothingstore.dto.request;

import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.ReturnRequestStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRequestProcessDTO {

  @NotNull(message = "return.request.id.not.null")
  private Long returnRequestId;

  @EnumValue(enumClass = ReturnRequestStatus.class, message = "return.request.status.invalid")
  private String status;

  private String adminComment;
}
