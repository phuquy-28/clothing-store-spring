package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDTO {

  private String email;

  private String firstName;

  private String lastName;

  private String role;

  private Long cartItemsCount;
}
