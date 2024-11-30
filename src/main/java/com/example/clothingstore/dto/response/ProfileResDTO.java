package com.example.clothingstore.dto.response;

import java.time.LocalDate;
import com.example.clothingstore.enumeration.Gender;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ProfileResDTO {

  private Long id;
  
  private String firstName;

  private String lastName;

  private LocalDate birthDate;

  private String phoneNumber;

  private Gender gender;
}
