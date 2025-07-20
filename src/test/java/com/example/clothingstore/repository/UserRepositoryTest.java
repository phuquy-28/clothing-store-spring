package com.example.clothingstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clothingstore.entity.Role;
import com.example.clothingstore.entity.User;
import jakarta.persistence.criteria.Join;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest
class UserRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private Role userRole;

  @BeforeEach
  void setUp() {
    // Create a role
    userRole = new Role();
    userRole.setName("ROLE_USER");
    entityManager.persist(userRole);

    // Create a test user
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPassword("password123");
    testUser.setActivated(true);
    testUser.setActivationKey("testActivationKey");
    testUser.setActivationCode("123456");
    testUser.setActivationCodeDate(Instant.now());
    testUser.setRole(userRole);
    testUser.setResetKey("testResetKey");
    testUser.setResetDate(Instant.now());
    testUser.setResetCode("123456");
    testUser.setCodeResetDate(Instant.now());

    entityManager.persist(testUser);
    entityManager.flush();
  }

  @Test
  @DisplayName("Should find user by email")
  void findByEmail_ShouldReturnUser_WhenEmailExists() {
    // When
    Optional<User> found = userRepository.findByEmail(testUser.getEmail());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
  }

  @Test
  @DisplayName("Should not find user by non-existent email")
  void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
    // When
    Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find user by activation key")
  void findByActivationKey_ShouldReturnUser_WhenKeyExists() {
    // When
    Optional<User> found = userRepository.findByActivationKey(testUser.getActivationKey());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getActivationKey()).isEqualTo(testUser.getActivationKey());
  }

  @Test
  @DisplayName("Should not find user by non-existent activation key")
  void findByActivationKey_ShouldReturnEmpty_WhenKeyDoesNotExist() {
    // When
    Optional<User> found = userRepository.findByActivationKey("nonexistentKey");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find user by activation key with lock")
  void findByActivationKeyWithLock_ShouldReturnUser_WhenKeyExists() {
    // When
    Optional<User> found = userRepository.findByActivationKeyWithLock(testUser.getActivationKey());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getActivationKey()).isEqualTo(testUser.getActivationKey());
  }

  @Test
  @DisplayName("Should not find user by non-existent activation key with lock")
  void findByActivationKeyWithLock_ShouldReturnEmpty_WhenKeyDoesNotExist() {
    // When
    Optional<User> found = userRepository.findByActivationKeyWithLock("nonexistentKey");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find active user by email")
  void findByEmailAndActivatedTrue_ShouldReturnUser_WhenEmailExistsAndUserActive() {
    // When
    Optional<User> found = userRepository.findByEmailAndActivatedTrue(testUser.getEmail());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().isActivated()).isTrue();
  }

  @Test
  @DisplayName("Should not find active user by email")
  void findByEmailAndActivatedTrue_ShouldReturnEmpty_WhenEmailDoesNotExist() {
    // When
    Optional<User> found = userRepository.findByEmailAndActivatedTrue("nonexistent@example.com");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find user by reset key")
  void findByResetKey_ShouldReturnUser_WhenKeyExists() {
    // When
    Optional<User> found = userRepository.findByResetKey(testUser.getResetKey());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getResetKey()).isEqualTo(testUser.getResetKey());
  }

  @Test
  @DisplayName("Should not find user by non-existent reset key")
  void findByResetKey_ShouldReturnEmpty_WhenKeyDoesNotExist() {
    // When
    Optional<User> found = userRepository.findByResetKey("nonexistentKey");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find user by email and activation code")
  void findByEmailAndActivationCode_ShouldReturnUser_WhenBothMatch() {
    // When
    Optional<User> found = userRepository.findByEmailAndActivationCode(testUser.getEmail(),
        testUser.getActivationCode());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getActivationCode()).isEqualTo(testUser.getActivationCode());
  }

  @Test
  @DisplayName("Should find user by Google ID")
  void findByGoogleId_ShouldReturnUser_WhenGoogleIdExists() {
    // Given
    testUser.setGoogleId("google123");
    entityManager.persist(testUser);
    entityManager.flush();

    // When
    Optional<User> found = userRepository.findByGoogleId(testUser.getGoogleId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getGoogleId()).isEqualTo(testUser.getGoogleId());
  }

  @Test
  @DisplayName("Should find users by role names")
  void findByRoleNameIn_ShouldReturnUsers_WhenRolesExist() {
    // Given
    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");
    entityManager.persist(adminRole);

    User adminUser = new User();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("admin123");
    adminUser.setRole(adminRole);
    entityManager.persist(adminUser);
    entityManager.flush();

    // When
    List<User> users = userRepository.findByRoleNameIn(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

    // Then
    assertThat(users).hasSize(2);
    assertThat(users).extracting("role.name").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }

  @Test
  @DisplayName("Should count activated users")
  void countByActivatedTrue_ShouldReturnCorrectCount() {
    // Given
    testUser.setActivated(true);
    entityManager.persist(testUser);

    User anotherUser = new User();
    anotherUser.setEmail("another@example.com");
    anotherUser.setPassword("password123");
    anotherUser.setActivated(true);
    anotherUser.setRole(userRole);
    entityManager.persist(anotherUser);
    entityManager.flush();

    // When
    Long count = userRepository.countByActivatedTrue();

    // Then
    assertThat(count).isEqualTo(2L);
  }

  @Test
  @DisplayName("Should find users with specification - by email pattern")
  void findAll_ShouldReturnFilteredUsers_WhenUsingEmailSpecification() {
    // Given
    User user2 = new User();
    user2.setEmail("test2@example.com");
    user2.setPassword("password123");
    user2.setActivated(true);
    user2.setRole(userRole);
    entityManager.persist(user2);

    User user3 = new User();
    user3.setEmail("other@example.com");
    user3.setPassword("password123");
    user3.setActivated(true);
    user3.setRole(userRole);
    entityManager.persist(user3);
    entityManager.flush();

    Specification<User> spec = (root, query, cb) -> cb.like(root.get("email"), "test%");

    PageRequest pageable = PageRequest.of(0, 10, Sort.by("email"));

    // When
    Page<User> result = userRepository.findAll(spec, pageable);

    // Then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).extracting("email")
        .containsExactlyInAnyOrder("test@example.com", "test2@example.com");
  }

  @Test
  @DisplayName("Should find users with specification - by role and activation status")
  void findAll_ShouldReturnFilteredUsers_WhenUsingRoleAndActivationSpecification() {
    // Given
    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");
    entityManager.persist(adminRole);

    User adminUser = new User();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("admin123");
    adminUser.setActivated(true);
    adminUser.setRole(adminRole);
    entityManager.persist(adminUser);

    User inactiveUser = new User();
    inactiveUser.setEmail("inactive@example.com");
    inactiveUser.setPassword("password123");
    inactiveUser.setActivated(false);
    inactiveUser.setRole(userRole);
    entityManager.persist(inactiveUser);
    entityManager.flush();

    Specification<User> spec = (root, query, cb) -> {
      Join<User, Role> roleJoin = root.join("role");
      return cb.and(cb.equal(root.get("activated"), true),
          cb.equal(roleJoin.get("name"), "ROLE_ADMIN"));
    };

    PageRequest pageable = PageRequest.of(0, 10);

    // When
    Page<User> result = userRepository.findAll(spec, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEmail()).isEqualTo("admin@example.com");
    assertThat(result.getContent().get(0).getRole().getName()).isEqualTo("ROLE_ADMIN");
  }

  @Test
  @DisplayName("Should find users with specification - empty result")
  void findAll_ShouldReturnEmptyPage_WhenNoUsersMatchSpecification() {
    // Given
    Specification<User> spec =
        (root, query, cb) -> cb.equal(root.get("email"), "nonexistent@example.com");

    PageRequest pageable = PageRequest.of(0, 10);

    // When
    Page<User> result = userRepository.findAll(spec, pageable);

    // Then
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  @Test
  @DisplayName("Should find users by multiple role names")
  void findByRoleNameIn_ShouldReturnUsers_WhenMultipleRolesExist() {
    // Given
    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");
    entityManager.persist(adminRole);

    Role managerRole = new Role();
    managerRole.setName("ROLE_MANAGER");
    entityManager.persist(managerRole);

    User adminUser = new User();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("admin123");
    adminUser.setRole(adminRole);
    entityManager.persist(adminUser);

    User managerUser = new User();
    managerUser.setEmail("manager@example.com");
    managerUser.setPassword("manager123");
    managerUser.setRole(managerRole);
    entityManager.persist(managerUser);
    entityManager.flush();

    // When
    List<User> users = userRepository
        .findByRoleNameIn(Arrays.asList("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_NONEXISTENT"));

    // Then
    assertThat(users).hasSize(2);
    assertThat(users).extracting("role.name").containsExactlyInAnyOrder("ROLE_ADMIN",
        "ROLE_MANAGER");
    assertThat(users).extracting("email").containsExactlyInAnyOrder("admin@example.com",
        "manager@example.com");
  }

  @Test
  @DisplayName("Should return empty list when no users found with given role names")
  void findByRoleNameIn_ShouldReturnEmptyList_WhenNoUsersWithGivenRoles() {
    // When
    List<User> users =
        userRepository.findByRoleNameIn(Arrays.asList("ROLE_NONEXISTENT", "ROLE_OTHER"));

    // Then
    assertThat(users).isEmpty();
  }
}
