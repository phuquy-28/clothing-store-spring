package com.example.clothingstore.client;

import com.example.clothingstore.dto.response.GoogleUserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "googleUserInfoClient", url = "${google.userinfo.base-url}")
public interface GoogleUserInfoClient {
  @GetMapping
  GoogleUserInfoDTO getUserInfo(@RequestParam("access_token") String accessToken);
}
