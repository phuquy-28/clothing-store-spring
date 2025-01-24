package com.example.clothingstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "users")
@ToString(exclude = {"shippingProfiles", "reviews", "refreshTokens", "defaultShippingProfile", "cart", "profile"})
@AllArgsConstructor
@NoArgsConstructor
public class User extends AbstractEntity {

  private String email;

  private String password;

  @ManyToOne
  @JoinColumn(name = "role_id")
  private Role role;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private Profile profile;

  @NotNull
  @Column(nullable = false)
  private boolean activated = false;

  @Size(max = 50)
  @Column(name = "activation_key", length = 50)
  @JsonIgnore
  private String activationKey;

  @Size(max = 6)
  private String activationCode;

  @Size(max = 50)
  @Column(name = "reset_key", length = 50)
  @JsonIgnore
  private String resetKey;

  @Column(name = "reset_date")
  private Instant resetDate = null;

  @Size(max = 6)
  private String resetCode;

  private Instant codeResetDate = null;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ShippingProfile> shippingProfiles;

  @OneToOne
  @JoinColumn(name = "default_shipping_profile_id")
  private ShippingProfile defaultShippingProfile;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Review> reviews;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserRefreshToken> refreshTokens;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Cart cart;

  @OneToMany(mappedBy = "user")
  private List<Order> orders;

  public User(String email, String password, boolean activated) {
    this.email = email;
    this.password = password;
    this.activated = activated;
  }
}
