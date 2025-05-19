package com.example.clothingstore.client;

import com.example.clothingstore.dto.response.GoogleTokenResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "googleAuthClient", url = "${google.auth.base-url}")
public interface GoogleAuthClient {
  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  GoogleTokenResponseDTO getTokenFromCode(@RequestBody Map<String, ?> formParams);
}
