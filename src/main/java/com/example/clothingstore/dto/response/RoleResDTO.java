package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResDTO {

  private Long id;

  private String name;
}