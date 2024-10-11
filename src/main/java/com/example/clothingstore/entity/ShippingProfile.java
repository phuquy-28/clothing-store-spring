package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "shipping_profiles")
@SQLDelete(sql = "UPDATE shipping_profiles SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingProfile extends AbstractEntity {

  private String firstName;

  private String lastName;

  private String phoneNumber;

  private String address;

  private String district;

  private String province;

  private String country;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  private boolean isDeleted = false;
}
