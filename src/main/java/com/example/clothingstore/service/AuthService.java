package com.example.clothingstore.service;

import com.example.clothingstore.domain.dto.request.auth.ReqLoginDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.response.auth.ResLoginDTO;
import com.example.clothingstore.domain.dto.response.user.ResCreateUser;
import com.example.clothingstore.utils.error.EmailInvalidException;
import com.example.clothingstore.utils.error.IdInvalidException;
import com.example.clothingstore.utils.error.TokenInvalidException;

public interface AuthService {

  ResCreateUser register(ReqRegisterDTO user) throws EmailInvalidException;

  ResLoginDTO login(ReqLoginDTO reqLoginDto);

  void logout();

  void activateAccount(String key);

  void sendActivationEmail(String email);

  ResLoginDTO refreshToken(String refreshToken) throws TokenInvalidException;
}
