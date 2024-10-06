package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResDTO {
  
  private Long id;

  private String firstName;

  private String lastName;

  private String birthDate;

  private String phoneNumber;

  private Gender gender;

  private String email;
}