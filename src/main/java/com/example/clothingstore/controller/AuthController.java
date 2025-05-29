package com.example.clothingstore.controller;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.ActivateAccountDTO;
import com.example.clothingstore.dto.request.GoogleAuthCodeRequestDTO;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.request.LogoutReqDTO;
import com.example.clothingstore.dto.request.RegisterReqDTO;
import com.example.clothingstore.dto.request.ReqEmailRecover;
import com.example.clothingstore.dto.request.ReqResetPassword;
import com.example.clothingstore.dto.request.ResetAccountDTO;
import com.example.clothingstore.dto.request.VerifyResetCodeDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.service.AuthService;
import com.example.clothingstore.util.SecurityUtil;
import com.example.clothingstore.exception.EmailInvalidException;
import com.example.clothingstore.exception.TokenInvalidException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuthController {

  private final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  private final SecurityUtil securityUtil;

  @PostMapping(UrlConfig.AUTH + UrlConfig.REGISTER)
  public ResponseEntity<RegisterResDTO> register(@RequestBody @Valid RegisterReqDTO user)
      throws EmailInvalidException {
    log.debug("REST request to register: {}", user);
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user));
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.LOGIN)
  public ResponseEntity<LoginResDTO> login(@RequestBody @Valid LoginReqDTO loginReqDto) {
    log.debug("REST request to login: {}", loginReqDto);
    LoginResDTO loginResDTO = authService.login(loginReqDto);

    if (loginResDTO.getUser().isActivated()) {
      // Create refresh token
      String refreshToken = securityUtil.createRefreshToken(loginReqDto.getEmail(), loginResDTO);

      // Create cookie
      ResponseCookie springCookie = ResponseCookie
          .from(AppConstant.REFRESH_TOKEN_COOKIE_NAME, refreshToken).httpOnly(true).secure(true)
          .path("/").maxAge(AppConstant.REFRESH_TOKEN_COOKIE_EXPIRE).sameSite("Lax").build();

      // Return response
      log.debug("Set refresh token cookie: {}", springCookie.toString());
      return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
          .body(loginResDTO);
    } else {
      return ResponseEntity.ok().body(loginResDTO);
    }

  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.GOOGLE_AUTH)
  public ResponseEntity<LoginResDTO> googleLogin(
      @RequestBody @Valid GoogleAuthCodeRequestDTO googleAuthCodeRequestDTO) {
    log.debug("REST request to login with Google: {}", googleAuthCodeRequestDTO);
    LoginResDTO loginResDTO = authService.loginWithGoogleAuth(googleAuthCodeRequestDTO.getCode());

    // Create refresh token
    String refreshToken =
        securityUtil.createRefreshToken(loginResDTO.getUser().getEmail(), loginResDTO);

    // Create cookie
    ResponseCookie springCookie = ResponseCookie
        .from(AppConstant.REFRESH_TOKEN_COOKIE_NAME, refreshToken).httpOnly(true).secure(true)
        .path("/").maxAge(AppConstant.REFRESH_TOKEN_COOKIE_EXPIRE).sameSite("Lax").build();

    // Return response
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
        .body(loginResDTO);
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.LOGOUT)
  public ResponseEntity<Void> logout(@RequestBody LogoutReqDTO logoutReqDTO,
      @CookieValue(name = AppConstant.REFRESH_TOKEN_COOKIE_NAME,
          required = false) String refreshToken) {
    log.debug("REST request to logout");

    ResponseCookie springCookie =
        ResponseCookie.from(AppConstant.REFRESH_TOKEN_COOKIE_NAME, "").httpOnly(true).secure(true)
            .path("/").maxAge(AppConstant.COOKIE_INVALID_EXPIRE).sameSite("Lax").build();

    authService.logout(refreshToken == null ? logoutReqDTO.getRefreshToken() : refreshToken);
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).build();
  }

  @GetMapping(UrlConfig.AUTH + UrlConfig.REFRESH)
  public ResponseEntity<LoginResDTO> refreshToken(
      @CookieValue(name = AppConstant.REFRESH_TOKEN_COOKIE_NAME,
          required = false) String refreshToken)
      throws TokenInvalidException {
    // log to debug refresh token
    log.debug("REST request to refresh token: {}", refreshToken);
    LoginResDTO loginResDTO = authService.refreshToken(refreshToken);

    // Tạo refresh token
    String newRefreshToken =
        securityUtil.createRefreshToken(loginResDTO.getUser().getEmail(), loginResDTO);

    // Tạo cookie
    ResponseCookie springCookie = ResponseCookie
        .from(AppConstant.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken).httpOnly(true).secure(true)
        .path("/").maxAge(AppConstant.REFRESH_TOKEN_COOKIE_EXPIRE).sameSite("Lax").build();

    // Trả về response
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString())
        .body(loginResDTO);
  }

  @GetMapping(UrlConfig.AUTH + UrlConfig.SEND_ACTIVATION_EMAIL)
  public ResponseEntity<Void> sendActivationEmail(@RequestParam("email") String email)
      throws EmailInvalidException {
    log.debug("REST request to send activation email: {}", email);
    authService.sendActivationEmail(email);
    return ResponseEntity.ok().build();
  }

  @GetMapping(UrlConfig.AUTH + UrlConfig.ACTIVATE)
  public ResponseEntity<LoginResDTO> activateAccount(@RequestParam("key") String key)
      throws TokenInvalidException {
    log.debug("REST request to activate account: {}", key);
    LoginResDTO res = authService.activateAccount(key);
    return ResponseEntity.ok().body(res);
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.RECOVER_PASSWORD)
  public ResponseEntity<Void> recoverPassword(@RequestBody @Valid ReqEmailRecover reqEmailRecover)
      throws EmailInvalidException {
    log.debug("REST request to recover password: {}", reqEmailRecover);
    authService.recoverPassword(reqEmailRecover.getEmail());
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.RESET_PASSWORD)
  public ResponseEntity<Void> resetPassword(@RequestParam("key") String key,
      @RequestBody @Valid ReqResetPassword reqResetPassword) throws TokenInvalidException {
    log.debug("REST request to reset password with key: {}", key);
    authService.resetPassword(key, reqResetPassword.getNewPassword(),
        reqResetPassword.getConfirmPassword());
    return ResponseEntity.ok().build();
  }

  @GetMapping(UrlConfig.AUTH + UrlConfig.SEND_ACTIVATION_CODE)
  public ResponseEntity<Void> sendActivationCode(@RequestParam("email") String email)
      throws EmailInvalidException {
    log.debug("REST request to send activation code: {}", email);
    authService.sendActivationCode(email);
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.ACTIVATE_CODE)
  public ResponseEntity<LoginResDTO> activateAccount(
      @RequestBody @Valid ActivateAccountDTO activateAccountDTO) throws TokenInvalidException {
    log.debug("REST request to activate account: {}", activateAccountDTO);
    LoginResDTO res = authService.activateAccount(activateAccountDTO.getEmail(),
        activateAccountDTO.getActivationCode());

    // Create refresh token
    String refreshToken = securityUtil.createRefreshToken(res.getUser().getEmail(), res);

    // Create cookie
    ResponseCookie springCookie = ResponseCookie
        .from(AppConstant.REFRESH_TOKEN_COOKIE_NAME, refreshToken).httpOnly(true).secure(true)
        .path("/").maxAge(AppConstant.REFRESH_TOKEN_COOKIE_EXPIRE).sameSite("Lax").build();

    // Return response
    log.debug("Set refresh token cookie: {}", springCookie.toString());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(res);
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.RECOVER_PASSWORD_CODE)
  public ResponseEntity<Void> recoverPasswordCode(
      @RequestBody @Valid ReqEmailRecover reqEmailRecover) throws EmailInvalidException {
    log.debug("REST request to recover password: {}", reqEmailRecover);
    authService.recoverPasswordCode(reqEmailRecover.getEmail());
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.VERIFY_RESET_CODE)
  public ResponseEntity<Void> verifyResetCode(
      @RequestBody @Valid VerifyResetCodeDTO verifyResetCodeDTO) {
    log.debug("REST request to verify email: {} with reset code: {}", verifyResetCodeDTO.getEmail(),
        verifyResetCodeDTO.getResetCode());
    authService.verifyResetCode(verifyResetCodeDTO.getEmail(), verifyResetCodeDTO.getResetCode());
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.AUTH + UrlConfig.RESET_PASSWORD_CODE)
  public ResponseEntity<Void> resetPasswordCode(
      @RequestBody @Valid ResetAccountDTO resetAccountDTO) {
    log.debug("REST request to reset email: {} with reset code: {}", resetAccountDTO.getEmail(),
        resetAccountDTO.getResetCode());
    authService.resetPassword(resetAccountDTO);
    return ResponseEntity.ok().build();
  }
}
