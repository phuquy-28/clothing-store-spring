package com.example.clothingstore.service.impl;

import com.example.clothingstore.repository.UserDeviceRepository;
import com.example.clothingstore.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.clothingstore.entity.User;
import com.example.clothingstore.entity.UserDevice;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.constant.ErrorMessage;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {
  
  private final Logger log = LoggerFactory.getLogger(UserDeviceServiceImpl.class);
  private final UserDeviceRepository userDeviceRepository;
  private final UserRepository userRepository;

  @Override
  public void saveFCMToken(String deviceToken, String email) {
    // Find user by email
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    // Check if device token already exists
    userDeviceRepository.findByDeviceToken(deviceToken)
        .ifPresent(existingDevice -> userDeviceRepository.delete(existingDevice));

    // Create and save new user device
    UserDevice userDevice = new UserDevice();
    userDevice.setDeviceToken(deviceToken);
    userDevice.setUser(user);
    userDeviceRepository.save(userDevice);
    
    log.debug("Saved FCM token for user {}: {}", email, deviceToken);
  }

  @Override
  public void deleteFCMToken(String deviceToken, String email) {
    // Find user by email
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    // Find and delete device token
    UserDevice userDevice = userDeviceRepository.findByDeviceToken(deviceToken)
        .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));

    // Verify the device belongs to the user
    if (!userDevice.getUser().getId().equals(user.getId())) {
      throw new ResourceNotFoundException("Device token not found for this user");
    }

    userDeviceRepository.delete(userDevice);
    log.debug("Deleted FCM token for user {}: {}", email, deviceToken);
  }
}
