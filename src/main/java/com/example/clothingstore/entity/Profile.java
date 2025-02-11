package com.example.clothingstore.entity;

import com.example.clothingstore.enumeration.Gender;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
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
@ToString(exclude = {"user"})
@AllArgsConstructor
@NoArgsConstructor
public class Profile extends AbstractEntity {

  private String firstName;

  private String lastName;

  private String fullName;

  private LocalDate birthDate;

  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private String avatar;

  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;
}
