package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultPaginationDTO {

  private Meta meta;

  private Object data;

  @Data
  @Builder
  public static class Meta {

    private Long page;

    private Long pageSize;

    private Long pages;

    private Long total;
  }
}
