package com.example.clothingstore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleUserInfoDTO {
  private String id;
  private String email;

  @JsonProperty("email_verified")
  private Boolean emailVerified;

  private String name;

  @JsonProperty("given_name")
  private String givenName;

  @JsonProperty("family_name")
  private String familyName;

  private String picture;
  private String locale;
}
