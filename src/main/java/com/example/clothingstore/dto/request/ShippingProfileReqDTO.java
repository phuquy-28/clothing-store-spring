package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShippingProfileReqDTO {

  private Long id;

  @NotBlank(message = "firstName.not.blank")
  private String firstName;

  @NotBlank(message = "lastName.not.blank")
  private String lastName;

  @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "phone.number.invalid")
  private String phoneNumber;

  @NotBlank(message = "address.not.blank")
  private String address;

  private Long wardId;

  @NotBlank(message = "ward.not.blank")
  private String ward;

  private Long districtId;

  @NotBlank(message = "district.not.blank")
  private String district;

  private Long provinceId;

  @NotBlank(message = "province.not.blank")
  private String province;

  @NotBlank(message = "country.not.blank")
  private String country;
}
