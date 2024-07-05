package com.example.clothingstore.domain.dto.response.user;

import com.example.clothingstore.domain.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResRegisterDTO {

  private Long id;

  private String email;

  private String firstName;

  private String lastName;
}
