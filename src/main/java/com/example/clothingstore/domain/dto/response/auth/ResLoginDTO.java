package com.example.clothingstore.domain.dto.response.auth;

import com.example.clothingstore.domain.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ResLoginDTO {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
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

    private long id;

    private String email;

    private String name;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RoleUser {

    private long id;

    private String name;
  }
}
