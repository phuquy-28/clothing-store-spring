package com.example.clothingstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name = "permissions")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends AbstractEntity {

  @NotBlank(message = "Permission name cannot be blank")
  private String name;

  @NotBlank(message = "API path cannot be blank")
  private String apiPath;

  @NotBlank(message = "Method cannot be blank")
  private String method;

  public Permission(String name, String apiPath, String method) {
    this.name = name;
    this.apiPath = apiPath;
    this.method = method;
  }

  @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
  @JsonIgnore
  private List<Role> roles;
}

