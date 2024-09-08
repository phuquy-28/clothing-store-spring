package com.example.clothingstore.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RegisterResDTO {

  private Long id;

  private String email;

  private String firstName;

  private String lastName;
}
