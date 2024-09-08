package com.example.clothingstore.service.impl;

import com.example.clothingstore.entity.User;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public User handleGetUserByUsername(String username) {
    if (userRepository.findByEmail(username).isPresent()) {
      return userRepository.findByEmail(username).get();
    }
    return null;
  }

  public void updateUserWithRefreshToken(User user, String refreshToken) {
    user.setRefreshToken(refreshToken);
    userRepository.save(user);
  }
}
