package com.example.clothingstore.config;

import com.example.clothingstore.domain.Address;
import com.example.clothingstore.domain.Customer;
import com.example.clothingstore.domain.Permission;
import com.example.clothingstore.domain.Role;
import com.example.clothingstore.domain.User;
import com.example.clothingstore.repository.PermissionRepository;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInitializer implements CommandLineRunner {

  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DatabaseInitializer(PermissionRepository permissionRepository,
      RoleRepository roleRepository, UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.permissionRepository = permissionRepository;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    System.out.println(">>> START INIT DATABASE");
    long countPermissions = this.permissionRepository.count();
    long countRoles = this.roleRepository.count();
    long countUsers = this.userRepository.count();

    if (countPermissions == 0) {
      ArrayList<Permission> arr = new ArrayList<>();
      arr.add(new Permission("Create a user", "/api/v1/users", "POST"));
      arr.add(new Permission("Update a user", "/api/v1/users", "PUT"));
      arr.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE"));
      arr.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET"));
      arr.add(new Permission("Get users with pagination", "/api/v1/users", "GET"));
      this.permissionRepository.saveAll(arr);
    }

    if (countRoles == 0) {
      // Create role ADMIN
      List<Permission> allPermissions = this.permissionRepository.findAll();
      Role adminRole = new Role();
      adminRole.setName("ADMIN");
      adminRole.setDescription("Admin has all permissions");
      adminRole.setActive(true);
      adminRole.setPermissions(allPermissions);
      this.roleRepository.save(adminRole);

      // Create role USER
      Role userRole = new Role();
      userRole.setName("USER");
      userRole.setDescription("User only read permissions");
      userRole.setActive(true);
      this.roleRepository.save(userRole);
    }

    if (countUsers == 0) {
      User adminUser = new User();
      adminUser.setEmail("admin@gmail.com");
      adminUser.setPassword(this.passwordEncoder.encode("123456"));
      adminUser.setActivated(true);

      // role
      Role adminRole = this.roleRepository.findByName("ADMIN").orElse(null);
      if (adminRole != null) {
        adminUser.setRole(adminRole);
      }

      // create customer
      Customer customer = new Customer();
      customer.setFirstName("Admin");
      customer.setLastName("Admin");
      adminUser.setCustomer(customer);

      // create address
      Address address = new Address();
      address.setAddressLine("76/3 Linh Trung, Thu Duc");
      address.setCity("Ho Chi Minh");
      address.setRegion("South");
      address.setCustomer(customer);

      customer.setAddress(address);

      this.userRepository.save(adminUser);
    }

    if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
      System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
    } else {
      System.out.println(">>> END INIT DATABASE");
    }
  }

}
