package com.example.clothingstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "addresses")
@AllArgsConstructor
@NoArgsConstructor
public class Address extends AbstractEntity {

  private String addressLine;

  private String city;

  private String region;

  @OneToOne(mappedBy = "address")
  @JsonIgnore
  private Customer customer;
}
