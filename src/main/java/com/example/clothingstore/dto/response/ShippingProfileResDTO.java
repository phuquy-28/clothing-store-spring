package com.example.clothingstore.dto.response;

import lombok.Data;

@Data
public class ShippingProfileResDTO {

  private Long id;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  private String address;

  private String ward;

  private String district;

  private String province;

  private String country;

  private boolean isDefault;
}
