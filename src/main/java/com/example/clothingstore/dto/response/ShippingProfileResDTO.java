package com.example.clothingstore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingProfileResDTO {

  private Long id;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  private String address;

  private Long wardId;

  private String ward;

  private Long districtId;

  private String district;

  private Long provinceId;

  private String province;

  private boolean isDefault;
}
