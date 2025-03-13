package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.RegisterReqDTO;
import com.example.clothingstore.dto.request.ResetAccountDTO;
import com.example.clothingstore.entity.Profile;
import com.example.clothingstore.entity.TokenBlacklist;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.Gender;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.mapper.UserMapper;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.TokenBlacklistRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.AuthService;
import com.example.clothingstore.service.EmailService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.RandomUtil;
import com.example.clothingstore.util.SecurityUtil;
import com.example.clothingstore.exception.BadCredentialsException;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.EmailInvalidException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.exception.TokenInvalidException;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final RoleRepository roleRepository;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  private final EmailService emailService;

  private final UserMapper userMapper;

  private final AuthenticationManager authenticationManager;

  private final TokenBlacklistRepository tokenBlacklistRepository;

  @Override
  public RegisterResDTO register(RegisterReqDTO user) throws EmailInvalidException {
    // check email is already in use
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new EmailInvalidException(ErrorMessage.EMAIL_EXISTED);
    }

    User newUser = new User();
    // email and password are required
    newUser.setEmail(user.getEmail());
    newUser.setPassword(passwordEncoder.encode(user.getPassword()));

    // set default role for new user
    newUser.setRole(roleRepository.findByName(AppConstant.ROLE_USER).orElse(null));

    // generate activation key and activation code
    newUser.setActivationKey(RandomUtil.generateActivationKey());
    newUser.setActivationCode(RandomUtil.generateActivationCode());

    // firstName and lastName are optional
    Profile profile = new Profile();
    profile.setFirstName(user.getFirstName());
    profile.setLastName(user.getLastName());
    profile.setFullName(String.format("%s %s", user.getLastName(), user.getFirstName()));
    if (user.getBirthDate() != null) {
      profile.setBirthDate(user.getBirthDate());
    }
    if (user.getPhone() != null) {
      profile.setPhoneNumber(user.getPhone());
    }
    if (user.getGender() != null) {
      profile.setGender(Gender.valueOf(user.getGender().toUpperCase()));
    }

    newUser.setProfile(profile);
    profile.setUser(newUser);

    User savedUser = userRepository.save(newUser);

    log.debug("Created Information for User: {}", savedUser);

    return userMapper.toRegisterResDTO(savedUser);
  }

  @Override
  public LoginResDTO login(LoginReqDTO loginReqDto) {
    // // Load input username/password into Security
    // UsernamePasswordAuthenticationToken authenticationToken = new
    // UsernamePasswordAuthenticationToken(
    // loginReqDto.getEmail(), loginReqDto.getPassword());

    // // Authenticate
    // Authentication authentication = authenticationManagerBuilder.getObject()
    // .authenticate(authenticationToken);
    // // SecurityContextHolder.getContext().setAuthentication(authentication);
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
          loginReqDto.getEmail(), loginReqDto.getPassword()));
    } catch (AuthenticationException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new BadCredentialsException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    // Get user information
    User loginUser = userService.handleGetUserByUsername(loginReqDto.getEmail());

    if (!AppConstant.ROLE_USER.equalsIgnoreCase(loginUser.getRole().getName())) {
      throw new BadCredentialsException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    // Create response
    LoginResDTO loginResDTO = convertUserToResLoginDTO(loginUser);

    if (loginUser.isActivated()) {
      // Tạo access token
      String accessToken = securityUtil.createAccessToken(loginUser, loginResDTO);
      loginResDTO.setAccessToken(accessToken);

      // Tạo refresh token
      String refreshToken = securityUtil.createRefreshToken(loginReqDto.getEmail(), loginResDTO);
      loginResDTO.setRefreshToken(refreshToken);

      userService.updateUserWithRefreshToken(loginUser, refreshToken);
    }

    log.debug("Login Information for User: {}", loginUser);

    return loginResDTO;
  }

  @Override
  public void logout(String refreshToken) {
    // Get current access token
    String token = SecurityUtil.getCurrentUserJWT().orElse(null);
    if (token == null) {
        throw new TokenInvalidException(ErrorMessage.ACCESS_TOKEN_INVALID);
    }

    // Validate refresh token
    try {
      securityUtil.jwtDecoder(refreshToken);
    } catch (Exception e) {
      throw new TokenInvalidException(ErrorMessage.REFRESH_TOKEN_INVALID);
    }

    // Add access token to blacklist
    Jwt jwt = securityUtil.jwtDecoder(token);
    TokenBlacklist blacklistToken = new TokenBlacklist();
    blacklistToken.setToken(token);
    blacklistToken.setCreatedDate(jwt.getIssuedAt());
    blacklistToken.setExpiryDate(jwt.getExpiresAt());
    tokenBlacklistRepository.save(blacklistToken);

    // Remove refresh token for current device
    String email = SecurityUtil.getCurrentUserLogin().orElse(null);
    if (email != null) {
        User user = userService.handleGetUserByUsername(email);
        user.getRefreshTokens().removeIf(rt -> rt.getRefreshToken().equals(refreshToken));
        userRepository.save(user);
    }
  }

  @Override
  public LoginResDTO activateAccount(String key) throws TokenInvalidException {
    if (userRepository.findByActivationKey(key).isPresent()) {
      // Sử dụng method mới với lock
      User user = userRepository.findByActivationKeyWithLock(key)
          .orElseThrow(() -> new TokenInvalidException(ErrorMessage.ACTIVATION_TOKEN_INVALID));
      user.setActivated(true);
      user.setActivationKey(null);
      User savedUser = userRepository.save(user);

      LoginResDTO loginResDTO = convertUserToResLoginDTO(savedUser);

      if (savedUser.isActivated()) {
        // Tạo access token
        String accessToken = securityUtil.createAccessToken(savedUser, loginResDTO);
        loginResDTO.setAccessToken(accessToken);

        // Tạo refresh token
        String refreshToken = securityUtil.createRefreshToken(savedUser.getEmail(), loginResDTO);
        loginResDTO.setRefreshToken(refreshToken);

        userService.updateUserWithRefreshToken(savedUser, refreshToken);
      }

      log.debug("Activated Information for User: {}", savedUser);
      return loginResDTO;
    } else {
      throw new TokenInvalidException(ErrorMessage.ACTIVATION_TOKEN_INVALID);
    }
  }

  @Override
  public void sendActivationEmail(String email) throws EmailInvalidException {
    if (userRepository.findByEmailAndActivatedFalse(email).isPresent()) {
      User user = userRepository.findByEmail(email).get();
      emailService.sendActivationEmail(user);
    } else {
      throw new EmailInvalidException(ErrorMessage.EMAIL_INVALID);
    }
  }

  @Override
  public LoginResDTO refreshToken(String refreshToken) throws TokenInvalidException {
    // Decode and validate refresh token
    Jwt jwt = securityUtil.jwtDecoder(refreshToken);
    String email = jwt.getSubject();
    
    // Get user and validate refresh token
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new TokenInvalidException(ErrorMessage.REFRESH_TOKEN_INVALID));
    
    // Check if refresh token exists and is not expired
    boolean validRefreshToken = user.getRefreshTokens().stream()
        .anyMatch(rt -> rt.getRefreshToken().equals(refreshToken) 
            && rt.getExpiryDate().isAfter(Instant.now()));
            
    if (!validRefreshToken) {
        throw new TokenInvalidException(ErrorMessage.REFRESH_TOKEN_INVALID);
    }

    // Create response
    LoginResDTO loginResDTO = convertUserToResLoginDTO(user);

    // Create new access token
    String accessToken = securityUtil.createAccessToken(user, loginResDTO);
    loginResDTO.setAccessToken(accessToken);

    // Create new refresh token
    String newRefreshToken = securityUtil.createRefreshToken(email, loginResDTO);
    loginResDTO.setRefreshToken(newRefreshToken);

    // Remove old refresh token and add new one
    user.getRefreshTokens().removeIf(rt -> rt.getRefreshToken().equals(refreshToken));
    userService.updateUserWithRefreshToken(user, newRefreshToken);

    return loginResDTO;
  }

  @Override
  public void recoverPassword(String email) throws EmailInvalidException {
    // check exist user with email
    User user = userRepository.findByEmailAndActivatedTrue(email)
        .orElseThrow(() -> new EmailInvalidException(ErrorMessage.EMAIL_INVALID));

    // check if the last password recovery request has expired (30 seconds)
    if (user.getResetDate() != null
        && user.getResetDate().isAfter(Instant.now().minusSeconds(30))) {
      throw new EmailInvalidException(ErrorMessage.PASSWORD_RECOVERY_TOO_FREQUENT);
    }

    // generate reset key and reset date
    user.setResetKey(RandomUtil.generateResetKey());
    user.setResetDate(Instant.now());

    log.debug("Reset password Information for User: {}", user);
    userRepository.save(user);

    emailService.sendRecoverPasswordEmail(user);
  }

  @Override
  public void resetPassword(String key, String newPassword, String confirmPassword)
      throws TokenInvalidException {
    // check exist user with reset key
    User user = userRepository.findByResetKey(key)
        .orElseThrow(() -> new TokenInvalidException(ErrorMessage.RESET_TOKEN_INVALID));

    // check reset key is expired
    if (user.getResetDate().isBefore(Instant.now().minusSeconds(60 * 15))) {
      throw new TokenInvalidException(ErrorMessage.RESET_TOKEN_INVALID);
    }

    // check new password and confirm password
    if (!newPassword.equals(confirmPassword)) {
      throw new TokenInvalidException(ErrorMessage.PASSWORD_NOT_MATCH);
    }

    // set new password
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setResetKey(null);
    user.setResetDate(null);

    log.debug("Reset password Information for User: {}", user);
    userRepository.save(user);
  }

  public static LoginResDTO convertUserToResLoginDTO(User loginUser) {
    LoginResDTO.ResUser resUser = new LoginResDTO.ResUser();
    resUser.setId(loginUser.getId());
    resUser.setEmail(loginUser.getEmail());
    if (loginUser.getProfile() != null) {
      resUser.setFirstName(loginUser.getProfile().getFirstName());
      resUser.setLastName(loginUser.getProfile().getLastName());
    }
    resUser.setActivated(loginUser.isActivated());
    // role is optional
    LoginResDTO.RoleUser roleUser = new LoginResDTO.RoleUser();
    if (loginUser.getRole() != null) {
      roleUser.setId(loginUser.getRole().getId());
      roleUser.setName(loginUser.getRole().getName());
      resUser.setRole(roleUser);
    }

    // create response
    LoginResDTO loginResDTO = new LoginResDTO();
    loginResDTO.setUser(resUser);
    return loginResDTO;
  }

  @Override
  public void sendActivationCode(String email) {
    if (userRepository.findByEmailAndActivatedFalse(email).isPresent()) {
      User user = userRepository.findByEmail(email).get();
      emailService.sendActivationCodeEmail(user);
    } else {
      throw new EmailInvalidException(ErrorMessage.EMAIL_INVALID);
    }
  }

  @Override
  public LoginResDTO activateAccount(String email, String activationCode) {
    Optional<User> userDb = userRepository.findByEmailAndActivationCode(email, activationCode);
    if (userDb.isPresent()) {
      User user = userDb.get();
      user.setActivated(true);
      user.setActivationCode(null);
      User savedUser = userRepository.save(user);

      LoginResDTO loginResDTO = convertUserToResLoginDTO(savedUser);

      if (savedUser.isActivated()) {
        // Tạo access token
        String accessToken = securityUtil.createAccessToken(savedUser, loginResDTO);
        loginResDTO.setAccessToken(accessToken);

        // Tạo refresh token
        String refreshToken = securityUtil.createRefreshToken(savedUser.getEmail(), loginResDTO);
        loginResDTO.setRefreshToken(refreshToken);

        userService.updateUserWithRefreshToken(savedUser, refreshToken);
      }

      log.debug("Activated Information for User: {}", savedUser);
      return loginResDTO;
    } else {
      throw new TokenInvalidException(ErrorMessage.ACTIVATION_CODE_INVALID);
    }
  }

  @Override
  public void recoverPasswordCode(String email) {
    // check exist user with email
    User user = userRepository.findByEmailAndActivatedTrue(email)
        .orElseThrow(() -> new EmailInvalidException(ErrorMessage.EMAIL_INVALID));

    // check if the last password recovery request has expired (30 seconds)
    if (user.getCodeResetDate() != null
        && user.getCodeResetDate().isAfter(Instant.now().minusSeconds(30))) {
      throw new EmailInvalidException(ErrorMessage.PASSWORD_RECOVERY_TOO_FREQUENT);
    }

    // generate reset key and reset date
    user.setResetCode(RandomUtil.generateResetCode());
    user.setCodeResetDate(Instant.now());

    log.debug("Reset password Information for User: {}", user);
    userRepository.save(user);

    emailService.sendResetCodeEmail(user);
  }

  @Override
  public void verifyResetCode(String email, String resetCode) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    if (!user.getResetCode().equals(resetCode)) {
      throw new BadRequestException(ErrorMessage.RESET_CODE_INVALID);
    }

    if (user.getCodeResetDate() != null
        && user.getCodeResetDate().isBefore(Instant.now().minusSeconds(60L * 15))) {
      throw new BadRequestException(ErrorMessage.RESET_CODE_EXPIRED);
    }
  }

  @Override
  public void resetPassword(ResetAccountDTO resetAccountDTO) {
    verifyResetCode(resetAccountDTO.getEmail(), resetAccountDTO.getResetCode());

    User user = userRepository.findByEmail(resetAccountDTO.getEmail())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    if (!resetAccountDTO.getNewPassword().equals(resetAccountDTO.getConfirmPassword())) {
      throw new BadRequestException(ErrorMessage.PASSWORD_NOT_MATCH);
    }

    user.setPassword(passwordEncoder.encode(resetAccountDTO.getNewPassword()));

    user.setResetCode(null);
    user.setCodeResetDate(null);

    userRepository.save(user);
  }
}

