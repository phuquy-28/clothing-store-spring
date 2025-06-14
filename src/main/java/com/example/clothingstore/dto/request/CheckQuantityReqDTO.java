package com.example.clothingstore.dto.request;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;

@Data
public class CheckQuantityReqDTO {

  @NotEmpty(message = "cartItemIds.not.empty")
  private List<Long> cartItemIds;
}
