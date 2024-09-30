package com.example.clothingstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "profiles")
@ToString(exclude = { "user" })
@AllArgsConstructor
@NoArgsConstructor
public class Profile extends AbstractEntity {

  private String firstName;

  private String lastName;

  private Instant birthDate;

  private String phoneNumber;

  @OneToOne(mappedBy = "profile")
  @JsonIgnore
  private User user;

  public Profile(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
