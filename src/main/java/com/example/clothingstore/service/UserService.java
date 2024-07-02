package com.example.clothingstore.service;

import com.example.clothingstore.domain.User;
import com.example.clothingstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User handleGetUserByUsername(String username) {
    if (userRepository.findByEmail(username).isPresent()) {
      return userRepository.findByEmail(username).get();
    }
    return null;
  }

  public void updateUserWithRefreshToken(User user, String refreshToken) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      User userToUpdate = userRepository.findByEmail(user.getEmail()).get();
      userToUpdate.setRefreshToken(refreshToken);
      userRepository.save(userToUpdate);
    }
  }
}
