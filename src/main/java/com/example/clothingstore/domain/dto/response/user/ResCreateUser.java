package com.example.clothingstore.domain.dto.response.user;

import com.example.clothingstore.domain.Customer;
import java.time.Instant;
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
public class ResCreateUser {

  private Long id;

  private String email;

  private Customer customer;

  private Instant createdAt;

  private String createdBy;

}
