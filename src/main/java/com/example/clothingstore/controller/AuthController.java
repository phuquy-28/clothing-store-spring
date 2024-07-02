package com.example.clothingstore.controller;

import com.example.clothingstore.domain.User;
import com.example.clothingstore.domain.dto.request.auth.ReqLoginDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.response.auth.ResLoginDTO;
import com.example.clothingstore.domain.dto.response.user.ResCreateUser;
import com.example.clothingstore.service.AuthService;
import com.example.clothingstore.utils.SecurityUtil;
import com.example.clothingstore.utils.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.version}")
public class AuthController {

  private final AuthService authService;

  private final SecurityUtil securityUtil;

  public AuthController(AuthService authService, SecurityUtil securityUtil) {
    this.authService = authService;
    this.securityUtil = securityUtil;
  }

  @PostMapping("/auth/register")
  @ApiMessage("Register success")
  public ResponseEntity<ResCreateUser> register(@RequestBody @Valid ReqRegisterDTO user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user));
  }

  @PostMapping("/auth/login")
  @ApiMessage("Login success")
  public ResponseEntity<ResLoginDTO> login(@RequestBody @Valid ReqLoginDTO reqLoginDto) {
    ResLoginDTO resLoginDTO = authService.login(reqLoginDto);

    // Tạo refresh token
    String refreshToken = securityUtil.createRefreshToken(reqLoginDto.getEmail(), resLoginDTO);

    // Tạo cookie
    ResponseCookie springCookie = ResponseCookie
        .from("refreshToken", refreshToken)
        .httpOnly(true).secure(true)
        .path("/")
        .maxAge(60 * 60 * 24 * 3)
        .build();

    // Trả về response
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
        .body(resLoginDTO);
  }
}
