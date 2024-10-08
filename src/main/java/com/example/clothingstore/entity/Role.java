package com.example.clothingstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Table(name = "roles")
@Entity
@ToString(exclude = {"permissions", "users"})
@AllArgsConstructor
@NoArgsConstructor
public class Role extends AbstractEntity {

  private String name;

  private String description;

  private boolean active;

  @ManyToMany(fetch = FetchType.LAZY)
  @JsonIgnoreProperties(value = {"roles"})
  @JoinTable(name = "permission_role", joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private List<Permission> permissions;

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  @JsonIgnore
  private List<User> users;

  public Role(String name, String description, boolean active, List<Permission> permissions) {
    this.name = name;
    this.description = description;
    this.active = active;
    this.permissions = permissions;
  }
}
