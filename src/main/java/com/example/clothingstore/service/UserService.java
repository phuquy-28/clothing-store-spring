package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.entity.User;

public interface UserService {

  User handleGetUserByUsername(String username);

  void updateUserWithRefreshToken(User user, String refreshToken);

  UserResDTO editProfile(EditProfileReqDTO editProfileReqDTO);
}
