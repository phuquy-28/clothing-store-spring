package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.RegisterReqDTO;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.exception.EmailInvalidException;
import com.example.clothingstore.exception.TokenInvalidException;

public interface AuthService {

  RegisterResDTO register(RegisterReqDTO user) throws EmailInvalidException;

  LoginResDTO login(LoginReqDTO loginReqDto);

  void logout();

  LoginResDTO activateAccount(String key) throws TokenInvalidException;

  void sendActivationEmail(String email) throws EmailInvalidException;

  LoginResDTO refreshToken(String refreshToken) throws TokenInvalidException;

  void recoverPassword(String email) throws EmailInvalidException;

  void resetPassword(String key, String newPassword, String confirmPassword)
      throws TokenInvalidException;
}
