package com.example.clothingstore.domain;

import com.example.clothingstore.utils.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name = "permissions")
@Entity
@NoArgsConstructor
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Permission name cannot be blank")
  private String name;

  @NotBlank(message = "API path cannot be blank")
  private String apiPath;

  @NotBlank(message = "Method cannot be blank")
  private String method;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  public Permission(String name, String apiPath, String method) {
    this.name = name;
    this.apiPath = apiPath;
    this.method = method;
  }

  @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
  @JsonIgnore
  private List<Role> roles;

  @PrePersist
  public void prePersist() {
    this.createdAt = Instant.now();
    SecurityUtil.getCurrentUserLogin().ifPresent(username -> this.createdBy = username);
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
    SecurityUtil.getCurrentUserLogin().ifPresent(username -> this.updatedBy = username);
  }
}

