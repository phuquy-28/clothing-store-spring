package com.example.clothingstore.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clothingstore.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

  Optional<UserDevice> findByDeviceToken(String deviceToken);

  List<UserDevice> findByUserId(Long userId);

}
