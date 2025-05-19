package com.example.clothingstore.service.impl;

import com.example.clothingstore.client.GoogleAuthClient;
import com.example.clothingstore.client.GoogleUserInfoClient;
import com.example.clothingstore.dto.response.GoogleTokenResponseDTO;
import com.example.clothingstore.dto.response.GoogleUserInfoDTO;
import com.example.clothingstore.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

  private final GoogleAuthClient googleAuthClient;
  private final GoogleUserInfoClient googleUserInfoClient;

  @Value("${google.auth.client-id}")
  private String clientId;

  @Value("${google.auth.client-secret}")
  private String clientSecret;

  @Value("${google.auth.redirect-uri}")
  private String redirectUri;

  @Override
  public GoogleTokenResponseDTO getTokensFromCode(String authorizationCode) {
    try {
      // Decode the authorization code
      String decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8.toString());

      Map<String, String> formParams = new HashMap<>();
      formParams.put("code", decodedCode);
      formParams.put("client_id", clientId);
      formParams.put("client_secret", clientSecret);
      formParams.put("redirect_uri", redirectUri);
      formParams.put("grant_type", "authorization_code");

      log.debug("Sending token request to Google with params: {}", formParams);
      return googleAuthClient.getTokenFromCode(formParams);
    } catch (UnsupportedEncodingException e) {
      log.error("Error decoding authorization code: {}", e.getMessage());
      throw new RuntimeException("Error decoding authorization code", e);
    }
  }

  @Override
  public GoogleUserInfoDTO getUserInfo(String accessToken) {
    return googleUserInfoClient.getUserInfo(accessToken);
  }
}
