package com.example.clothingstore.dto.response;

import java.time.LocalDate;
import com.example.clothingstore.enumeration.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResMobileDTO {

  private Long id;
  
  private String firstName;

  private String lastName;

  private LocalDate birthDate;

  private String phoneNumber;

  private Gender gender;

  private String email;

  private String avatar;
}
