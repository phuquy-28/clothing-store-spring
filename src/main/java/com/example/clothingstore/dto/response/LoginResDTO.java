package com.example.clothingstore.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
public class LoginResDTO {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  @JsonIgnore
  private String refreshToken;

  private ResUser user;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  static public class ResUser {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private boolean activated;

    private RoleUser role;
  }

//  @Getter
//  @Setter
//  @AllArgsConstructor
//  @NoArgsConstructor
//  static public class InnerUser {
//
//    private ResUser user;
//  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UserInsideToken {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RoleUser {

    private Long id;

    private String name;
  }
}
