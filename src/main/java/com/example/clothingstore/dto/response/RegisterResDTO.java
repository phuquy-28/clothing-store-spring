package com.example.clothingstore.dto.response;

import lombok.Data;

@Data
public class RegisterResDTO {

  private Long id;

  private String email;

  private String firstName;

  private String lastName;
}
