package com.example.clothingstore.service;

import com.example.clothingstore.domain.dto.request.auth.ReqLoginDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.response.auth.ResLoginDTO;
import com.example.clothingstore.domain.dto.response.user.ResRegisterDTO;
import com.example.clothingstore.utils.error.EmailInvalidException;
import com.example.clothingstore.utils.error.TokenInvalidException;

public interface AuthService {

  ResRegisterDTO register(ReqRegisterDTO user) throws EmailInvalidException;

  ResLoginDTO login(ReqLoginDTO reqLoginDto);

  void logout();

  ResLoginDTO activateAccount(String key) throws TokenInvalidException;

  void sendActivationEmail(String email) throws EmailInvalidException;

  ResLoginDTO refreshToken(String refreshToken) throws TokenInvalidException;
}
