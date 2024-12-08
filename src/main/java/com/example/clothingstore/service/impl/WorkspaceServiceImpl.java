package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.exception.BadCredentialsException;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.service.WorkspaceService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

  private final Logger log = LoggerFactory.getLogger(WorkspaceServiceImpl.class);

  private final AuthenticationManager authenticationManager;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  @Override
  public LoginResDTO login(LoginReqDTO loginReqDto) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
          loginReqDto.getEmail(), loginReqDto.getPassword()));
    } catch (AuthenticationException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new BadCredentialsException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    // Get user information
    User loginUser = userService.handleGetUserByUsername(loginReqDto.getEmail());

    // Check if user has admin role (not USER role)
    if (AppConstant.ROLE_USER.equalsIgnoreCase(loginUser.getRole().getName())) {
      throw new BadCredentialsException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    // Create response
    LoginResDTO loginResDTO = AuthServiceImpl.convertUserToResLoginDTO(loginUser);

    if (loginUser.isActivated()) {
      String accessToken = securityUtil.createAccessToken(loginUser, loginResDTO);
      loginResDTO.setAccessToken(accessToken);

      String refreshToken = securityUtil.createRefreshToken(loginReqDto.getEmail(), loginResDTO);
      loginResDTO.setRefreshToken(refreshToken);

      userService.updateUserWithRefreshToken(loginUser, refreshToken);
    }

    return loginResDTO;
  }


}
