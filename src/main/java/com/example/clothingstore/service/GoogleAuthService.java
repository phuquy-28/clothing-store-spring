package com.example.clothingstore.service;

import com.example.clothingstore.dto.response.GoogleTokenResponseDTO;
import com.example.clothingstore.dto.response.GoogleUserInfoDTO;

public interface GoogleAuthService {
  GoogleTokenResponseDTO getTokensFromCode(String authorizationCode);

  GoogleUserInfoDTO getUserInfo(String accessToken);
}
