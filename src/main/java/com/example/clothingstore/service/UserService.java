package com.example.clothingstore.service;

import com.example.clothingstore.entity.User;

public interface UserService {

  User handleGetUserByUsername(String username);

  void updateUserWithRefreshToken(User user, String refreshToken);
}
