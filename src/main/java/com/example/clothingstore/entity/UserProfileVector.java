package com.example.clothingstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "user_profile_vectors")
@Getter
@Setter
@NoArgsConstructor
public class UserProfileVector {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Lob
  @Column(name = "vector", nullable = false, columnDefinition = "LONGTEXT")
  private String vector; // Lưu vector dưới dạng chuỗi JSON

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // Constructor để tạo entity mới với User
  public UserProfileVector(User user) {
    this.user = user;
    this.updatedAt = Instant.now();
  }
}
