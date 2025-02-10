package com.example.clothingstore.dto.request;

import java.time.LocalDate;
import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileMobileReqDTO {

  @NotBlank(message = "first.name.not.blank")
  private String firstName;

  @NotBlank(message = "last.name.not.blank")
  private String lastName;

  @EnumValue(enumClass = Gender.class, message = "gender.invalid")
  private String gender;

  private LocalDate birthDate;

  @NotBlank(message = "email.not.blank")
  @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "email.not.valid")
  private String email;

  @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "phone.number.invalid")
  private String phoneNumber;

  private String otp;
}

