package com.example.clothingstore.entity;

import com.example.clothingstore.enumeration.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

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

  private LocalDate birthDate;

  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @OneToOne(mappedBy = "profile")
  @JsonIgnore
  private User user;

  public Profile(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
