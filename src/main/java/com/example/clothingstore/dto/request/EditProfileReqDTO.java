package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.Gender;

@Data
public class EditProfileReqDTO {

  @NotBlank(message = "first.name.not.blank")
  private String firstName;

  @NotBlank(message = "last.name.not.blank")
  private String lastName;

  private LocalDate birthDate;

  @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "phone.number.invalid")
  private String phoneNumber;

  @EnumValue(enumClass = Gender.class, message = "gender.invalid")
  private String gender;
}
