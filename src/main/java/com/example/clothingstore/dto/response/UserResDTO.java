package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResDTO {
  
  private Long id;

  private String email;

  private String firstName;

  private String lastName;

  private String fullName;

  private String birthDate;

  private String phoneNumber;

  private Gender gender;

  private RoleResDTO role;

}
