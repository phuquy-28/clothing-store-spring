package com.example.clothingstore.config;

import com.example.clothingstore.entity.Profile;
import com.example.clothingstore.entity.Permission;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.Role;
import com.example.clothingstore.entity.ShippingProfile;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.Gender;
import com.example.clothingstore.repository.PermissionRepository;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.service.PineconeService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
@Service
public class DatabaseInitializer implements CommandLineRunner {

  private final PermissionRepository permissionRepository;

  private final RoleRepository roleRepository;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final ProductVariantRepository productVariantRepository;

  private final ProductRepository productRepository;

  private final PineconeService pineconeService;

  @Value("${pinecone.namespace}")
  private String PRODUCT_NAMESPACE;

  @Override
  public void run(String... args) throws Exception {
    System.out.println(">>> START INIT DATABASE");
    long countPermissions = this.permissionRepository.count();
    long countRoles = this.roleRepository.count();
    long countUsers = this.userRepository.count();

    if (countPermissions == 0) {
      initializePermissions();
    }

    if (countRoles == 0) {
      initializeRoles();
    }

    if (countUsers == 0) {
      initializeUsers();
    }

    // Generate missing SKUs for product variants
    this.generateMissingSkus();

    // Initialize Pinecone vectors
    this.initializePineconeVectors();

    if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
      System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
    } else {
      System.out.println(">>> END INIT DATABASE");
    }
  }

  private void initializePermissions() {
    ArrayList<Permission> arr = new ArrayList<>();
    arr.add(new Permission("CREATE_USER", "/api/v1/users", "POST"));
    arr.add(new Permission("UPDATE_USER", "/api/v1/users", "PUT"));
    arr.add(new Permission("DELETE_USER", "/api/v1/users/{id}", "DELETE"));
    arr.add(new Permission("GET_USER", "/api/v1/users/{id}", "GET"));
    arr.add(new Permission("GET_USERS", "/api/v1/users", "GET"));
    this.permissionRepository.saveAll(arr);
  }

  private void initializeRoles() {
    List<Permission> allPermissions = this.permissionRepository.findAll();
      
    List<Role> roles = new ArrayList<>();
    roles.add(new Role("ADMIN", "Admin has all permissions", true, allPermissions));
    roles.add(new Role("USER", "User has specific permissions", true, new ArrayList<>()));
    roles.add(new Role("STAFF", "Staff has specific permissions", true, new ArrayList<>()));
    roles.add(new Role("MANAGER", "Manager has specific permissions", true, new ArrayList<>()));
    
    this.roleRepository.saveAll(roles);
  }

  private void initializeUsers() {
    Role adminRole = this.roleRepository.findByName("ADMIN").orElse(null);
    Role userRole = this.roleRepository.findByName("USER").orElse(null);
    
    List<User> users = new ArrayList<>();
    
    // Create admin user
    User adminUser = createAdminUser(adminRole);
    users.add(adminUser);
    
    // Create normal user
    User normalUser = createNormalUser(userRole);
    users.add(normalUser);
    
    // Save all users in one batch
    this.userRepository.saveAll(users);
  }

  private User createAdminUser(Role adminRole) {
    User adminUser = new User("admin@gmail.com", this.passwordEncoder.encode("123456"), true);
    adminUser.setRole(adminRole);
    
    Profile adminProfile = new Profile(
        "Admin",
        "System",
        String.format("%s %s", "System", "Admin"),
        LocalDate.of(1990, 1, 1),
        "0909090909",
        Gender.MALE,
        "https://res.cloudinary.com/db9vcatme/image/upload/v1739239313/default_hmodfn.png",
        adminUser
    );
    adminUser.setProfile(adminProfile);
    
    ShippingProfile adminShippingProfile = new ShippingProfile(
        "Admin",
        "System",
        "0909090909",
        "123 Admin Street",
        90737L,    // wardId
        "Phường Linh Trung",
        3695L,    // districtId
        "Thành Phố Thủ Đức",
        202L,    // provinceId
        "Hồ Chí Minh",
        adminUser
    );
    adminUser.setShippingProfiles(List.of(adminShippingProfile));
    adminUser.setDefaultShippingProfile(adminShippingProfile);
    
    return adminUser;
  }

  private User createNormalUser(Role userRole) {
    User normalUser = new User("phuquy2823@gmail.com", this.passwordEncoder.encode("123456"), true);
    normalUser.setRole(userRole);
    
    Profile userProfile = new Profile(
        "Normal",
        "User",
        String.format("%s %s", "User", "Normal"),
        LocalDate.of(1995, 6, 15),
        "0901234567",
        Gender.FEMALE,
        "https://res.cloudinary.com/db9vcatme/image/upload/v1739239313/default_hmodfn.png",
        normalUser
    );
    normalUser.setProfile(userProfile);
    
    ShippingProfile userShippingProfile = new ShippingProfile(
        "Normal",
        "User",
        "0901234567",
        "456 User Street",
        90737L, // wardId
        "Phường Linh Trung",
        3695L, // districtId
        "Thành Phố Thủ Đức",
        202L, // provinceId
        "Hồ Chí Minh",
        normalUser
    );
    normalUser.setShippingProfiles(List.of(userShippingProfile));
    normalUser.setDefaultShippingProfile(userShippingProfile);
    
    return normalUser;
  }

  private void generateMissingSkus() {
    System.out.println(">>> START GENERATING MISSING MEANINGFUL SKUS FOR VARIANTS");
    List<ProductVariant> variantsWithoutSku =
        productVariantRepository
            .findAll().stream().filter(variant -> variant.getSku() == null
                || variant.getSku().isBlank() || variant.getSku().startsWith("SKU-"))
            .collect(Collectors.toList());

    if (!variantsWithoutSku.isEmpty()) {
      for (ProductVariant variant : variantsWithoutSku) {
        String colorCode =
            variant.getColor().name().substring(0, Math.min(3, variant.getColor().name().length()));
        String sku = String
            .format("%d-%s-%s", variant.getProduct().getId(), colorCode, variant.getSize().name())
            .toUpperCase();
        variant.setSku(sku);
      }
      productVariantRepository.saveAll(variantsWithoutSku);
      System.out
          .println(">>> Generated/Updated " + variantsWithoutSku.size() + " meaningful SKUs.");
    } else {
      System.out.println(">>> No missing or old-format SKUs to generate.");
    }
    System.out.println(">>> END GENERATING MISSING SKUS");
  }

  private void initializePineconeVectors() {
    System.out.println(">>> START INITIALIZING PINECONE VECTORS");
    System.out.println("Checking Pinecone status for namespace '" + PRODUCT_NAMESPACE + "'");
    int vectorCount = pineconeService.getNamespaceVectorCount(PRODUCT_NAMESPACE);

    if (vectorCount > 0) {
      System.out.println("Namespace '" + PRODUCT_NAMESPACE + "' already contains " + vectorCount + " vectors. Skipping initial bulk indexing.");
      return;
    }

    if (vectorCount == 0) {
      System.out.println("Namespace '" + PRODUCT_NAMESPACE + "' is empty. Starting initial product indexing...");
      // Eager fetch products with variants to avoid LazyInitializationException
      List<Product> allProducts = productRepository.findAllWithVariants();
      allProducts.forEach(pineconeService::indexSingleProduct);
      System.out.println("Finished initial indexing of " + allProducts.size() + " products into namespace '" + PRODUCT_NAMESPACE + "'.");
    } else {
      System.out.println("Could not verify Pinecone index status. Initial indexing is paused.");
    }
    System.out.println(">>> END INITIALIZING PINECONE VECTORS");
  }
}
