package com.example.clothingstore.config;

import com.example.clothingstore.entity.Profile;
import com.example.clothingstore.entity.Permission;
import com.example.clothingstore.entity.Role;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.repository.PermissionRepository;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DatabaseInitializer implements CommandLineRunner {

  private final PermissionRepository permissionRepository;

  private final RoleRepository roleRepository;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    System.out.println(">>> START INIT DATABASE");
    long countPermissions = this.permissionRepository.count();
    long countRoles = this.roleRepository.count();
    long countUsers = this.userRepository.count();

    if (countPermissions == 0) {
      ArrayList<Permission> arr = new ArrayList<>();
      arr.add(new Permission("CREATE_USER", "/api/v1/users", "POST"));
      arr.add(new Permission("UPDATE_USER", "/api/v1/users", "PUT"));
      arr.add(new Permission("DELETE_USER", "/api/v1/users/{id}", "DELETE"));
      arr.add(new Permission("GET_USER", "/api/v1/users/{id}", "GET"));
      arr.add(new Permission("GET_USERS", "/api/v1/users", "GET"));
      this.permissionRepository.saveAll(arr);
    }

    if (countRoles == 0) {
      // Create role ADMIN
      List<Permission> allPermissions = this.permissionRepository.findAll();
      Role adminRole = new Role("ADMIN", "Admin has all permissions", true, allPermissions);
      this.roleRepository.save(adminRole);

      // Create role USER
      Role userRole = new Role("USER", "User has specific permissions", true, new ArrayList<>());
      this.roleRepository.save(userRole);
    }

    if (countUsers == 0) {
      // Create user admin
      User adminUser = new User("admin@gmail.com", this.passwordEncoder.encode("123456"), true);

      // role
      Role adminRole = this.roleRepository.findByName("ADMIN").orElse(null);
      if (adminRole != null) {
        adminUser.setRole(adminRole);
      }

      // create profile
      Profile profile = new Profile("Admin", "Admin");
      adminUser.setProfile(profile);

      this.userRepository.save(adminUser);
    }

    if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
      System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
    } else {
      System.out.println(">>> END INIT DATABASE");
    }
  }

}
