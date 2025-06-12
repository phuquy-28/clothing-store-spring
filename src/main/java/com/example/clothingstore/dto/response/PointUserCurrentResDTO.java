package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointUserCurrentResDTO {

  private Long id;

  private Long currentPoints;

  private Long totalAccumulatedPoints;

}
