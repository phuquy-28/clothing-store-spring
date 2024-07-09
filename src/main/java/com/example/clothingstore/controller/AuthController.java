package com.example.clothingstore.controller;

import com.example.clothingstore.domain.dto.request.auth.ReqEmailRecover;
import com.example.clothingstore.domain.dto.request.auth.ReqLoginDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqResetPassword;
import com.example.clothingstore.domain.dto.response.auth.ResLoginDTO;
import com.example.clothingstore.domain.dto.response.user.ResRegisterDTO;
import com.example.clothingstore.service.AuthService;
import com.example.clothingstore.utils.SecurityUtil;
import com.example.clothingstore.utils.annotation.ApiMessage;
import com.example.clothingstore.utils.error.EmailInvalidException;
import com.example.clothingstore.utils.error.TokenInvalidException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.version}")
public class AuthController {

  private final Logger log = LoggerFactory.getLogger(AuthController.class);
  private final AuthService authService;
  private final SecurityUtil securityUtil;

  public AuthController(AuthService authService, SecurityUtil securityUtil) {
    this.authService = authService;
    this.securityUtil = securityUtil;
  }

  @PostMapping("/auth/register")
  @ApiMessage("Register success")
  public ResponseEntity<ResRegisterDTO> register(@RequestBody @Valid ReqRegisterDTO user)
      throws EmailInvalidException {
    log.debug("REST request to register: {}", user);
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user));
  }

  @PostMapping("/auth/login")
  @ApiMessage("Login success")
  public ResponseEntity<ResLoginDTO> login(@RequestBody @Valid ReqLoginDTO reqLoginDto) {
    log.debug("REST request to login: {}", reqLoginDto);
    ResLoginDTO resLoginDTO = authService.login(reqLoginDto);

    if (resLoginDTO.getUser().isActivated()) {
      // Tạo refresh token
      String refreshToken = securityUtil.createRefreshToken(reqLoginDto.getEmail(), resLoginDTO);

      // Tạo cookie
      ResponseCookie springCookie = ResponseCookie
          .from("refresh-token", refreshToken)
          .httpOnly(true).secure(true)
          .path("/")
          .maxAge(60 * 60 * 24 * 3)
          .build();

      // Trả về response
      log.debug("Set refresh token cookie: {}", springCookie.toString());
      return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
          .body(resLoginDTO);
    } else {
      return ResponseEntity.ok().body(resLoginDTO);
    }

  }

  @PostMapping("/auth/logout")
  @ApiMessage("Logout success")
  public ResponseEntity<Void> logout() {
    log.debug("REST request to logout");
    ResponseCookie springCookie = ResponseCookie
        .from("refresh-token", "")
        .httpOnly(true).secure(true)
        .path("/")
        .maxAge(0)
        .build();

    authService.logout();
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).build();
  }

  @GetMapping("auth/refresh")
  @ApiMessage("Refresh token successfully")
  public ResponseEntity<ResLoginDTO> refreshToken(
      @CookieValue(name = "refresh-token") String refreshToken) throws TokenInvalidException {
    // log to debug refresh token
    log.debug("REST request to refresh token: {}", refreshToken);
    ResLoginDTO resLoginDTO = authService.refreshToken(refreshToken);

    // Tạo refresh token
    String newRefreshToken = securityUtil.createRefreshToken(resLoginDTO.getUser().getEmail(),
        resLoginDTO);

    // Tạo cookie
    ResponseCookie springCookie = ResponseCookie
        .from("refresh-token", newRefreshToken)
        .httpOnly(true).secure(true)
        .path("/")
        .maxAge(60 * 60 * 24 * 3)
        .build();

    // Trả về response
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
        .body(resLoginDTO);
  }

  @GetMapping("auth/send-activation-email")
  @ApiMessage("Send activation email successfully")
  public ResponseEntity<Void> sendActivationEmail(@RequestParam("email") String email)
      throws EmailInvalidException {
    log.debug("REST request to send activation email: {}", email);
    authService.sendActivationEmail(email);
    return ResponseEntity.ok().build();
  }

  @GetMapping("auth/activate")
  @ApiMessage("Activate account successfully")
  public ResponseEntity<ResLoginDTO> activateAccount(@RequestParam("key") String key)
      throws TokenInvalidException {
    log.debug("REST request to activate account: {}", key);
    ResLoginDTO res = authService.activateAccount(key);
    return ResponseEntity.ok().body(res);
  }

  @PostMapping("auth/recovery-password")
  @ApiMessage("Sent recover password email successfully")
  public ResponseEntity<Void> recoverPassword(@RequestBody @Valid ReqEmailRecover reqEmailRecover)
      throws EmailInvalidException {
    log.debug("REST request to recover password: {}", reqEmailRecover);
    authService.recoverPassword(reqEmailRecover.getEmail());
    return ResponseEntity.ok().build();
  }

  @PostMapping("auth/reset-password")
  @ApiMessage("Reset password successfully")
  public ResponseEntity<Void> resetPassword(@RequestParam("key") String key,
      @RequestBody @Valid ReqResetPassword reqResetPassword)
      throws TokenInvalidException {
    log.debug("REST request to reset password with key: {}", key);
    authService.resetPassword(key, reqResetPassword.getNewPassword(), reqResetPassword.getConfirmPassword());
    return ResponseEntity.ok().build();
  }
}
