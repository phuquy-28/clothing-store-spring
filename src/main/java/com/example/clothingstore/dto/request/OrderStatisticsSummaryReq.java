package com.example.clothingstore.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatisticsSummaryReq {

  @NotNull(message = "start.date.not.null")
  LocalDate startDate;

  @NotNull(message = "end.date.not.null")
  LocalDate endDate;
}
