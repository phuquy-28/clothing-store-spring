package com.example.clothingstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import com.example.clothingstore.entity.Role;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.entity.UserDevice;

@DataJpaTest
public class UserDeviceRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserDeviceRepository userDeviceRepository;

  private UserDevice testUserDevice;
  private User testUser;
  private Role userRole;

  @BeforeEach
  void setUp() {
    // Create and persist Role first
    userRole = new Role();
    userRole.setName("ROLE_USER");
    userRole.setDescription("User role");
    userRole.setActive(true);
    entityManager.persist(userRole);

    // Create and persist User
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPassword("password123");
    testUser.setActivated(true);
    testUser.setRole(userRole);
    entityManager.persist(testUser);

    // Create and persist UserDevice
    testUserDevice = new UserDevice();
    testUserDevice.setUser(testUser);
    testUserDevice.setDeviceToken("testDeviceToken");
    entityManager.persist(testUserDevice);

    // Flush all changes to ensure everything is saved
    entityManager.flush();
  }

  @Test
  @DisplayName("Should find user device by device token")
  void findByDeviceToken_ShouldReturnUserDevice_WhenDeviceTokenExists() {
    // When
    Optional<UserDevice> found =
        userDeviceRepository.findByDeviceToken(testUserDevice.getDeviceToken());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getDeviceToken()).isEqualTo(testUserDevice.getDeviceToken());
  }

  @Test
  @DisplayName("Should not find user device with non-existent token")
  void findByDeviceToken_ShouldReturnEmpty_WhenDeviceTokenDoesNotExist() {
    // When
    Optional<UserDevice> found = userDeviceRepository.findByDeviceToken("nonexistentToken");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find user devices by user ID")
  void findByUserId_ShouldReturnUserDevices_WhenUserExists() {
    // When
    List<UserDevice> found = userDeviceRepository.findByUserId(testUser.getId());

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getUser().getId()).isEqualTo(testUser.getId());
  }

  @Test
  @DisplayName("Should not find user devices when user does not exist")
  void findByUserId_ShouldReturnEmpty_WhenUserDoesNotExist() {
    // When
    List<UserDevice> found = userDeviceRepository.findByUserId(999999999999999999L);

    // Then
    assertThat(found).isEmpty();
  }
}
