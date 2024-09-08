package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.RegisterReqDTO;
import com.example.clothingstore.entity.Customer;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.mapper.UserMapper;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.AuthService;
import com.example.clothingstore.service.EmailService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.RandomUtil;
import com.example.clothingstore.util.SecurityUtil;
import com.example.clothingstore.exception.EmailInvalidException;
import com.example.clothingstore.exception.TokenInvalidException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuthServiceImpl implements AuthService {

  private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final RoleRepository roleRepository;

  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  private final EmailService emailService;

  private final UserMapper userMapper;

  @Override
  public RegisterResDTO register(RegisterReqDTO user) throws EmailInvalidException {
    // check email is already in use
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new EmailInvalidException("Email này đã được sử dụng");
    }

    User newUser = new User();
    // email and password are required
    newUser.setEmail(user.getEmail());
    newUser.setPassword(passwordEncoder.encode(user.getPassword()));

    // set default role for new user
    newUser.setRole(roleRepository.findByName(AppConstant.ROLE_USER).orElse(null));

    // generate activation key
    newUser.setActivationKey(RandomUtil.generateActivationKey());

    // firstName and lastName are optional
    Customer customer = new Customer();
    if (user.getFirstName() != null) {
      customer.setFirstName(user.getFirstName());
    }
    if (user.getLastName() != null) {
      customer.setLastName(user.getLastName());
    }

    newUser.setCustomer(customer);

    User savedUser = userRepository.save(newUser);

    log.debug("Created Information for User: {}", savedUser);

    return userMapper.toRegisterResDTO(savedUser);
  }

  @Override
  public LoginResDTO login(LoginReqDTO loginReqDto) {
    // Load input username/password into Security
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        loginReqDto.getEmail(), loginReqDto.getPassword());

    // Authenticate
    Authentication authentication = authenticationManagerBuilder.getObject()
        .authenticate(authenticationToken);
//    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Get user information
    User loginUser = userService.handleGetUserByUsername(loginReqDto.getEmail());

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
  public void logout() {
    // get email
    String email =
        SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
            : null;
    // get user
    User currentUserDB = userService.handleGetUserByUsername(email);
    // update user with refresh token
    userService.updateUserWithRefreshToken(currentUserDB, null);
    log.debug("Logout Information for User: {}", currentUserDB);
  }

  @Override
  public LoginResDTO activateAccount(String key) throws TokenInvalidException {
    if (userRepository.findByActivationKey(key).isPresent()) {
      User user = userRepository.findByActivationKey(key).get();
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
    Jwt jwt = securityUtil.jwtDecoder(refreshToken);

    String email = jwt.getSubject();

    // check exist user with email and refresh token
    User loginUser = userRepository.findByEmailAndRefreshToken(email, refreshToken)
        .orElseThrow(() -> new TokenInvalidException(ErrorMessage.REFRESH_TOKEN_INVALID));

    // Tạo response
    LoginResDTO loginResDTO = convertUserToResLoginDTO(loginUser);

    // Tạo access token
    String accessToken = securityUtil.createAccessToken(loginUser, loginResDTO);
    loginResDTO.setAccessToken(accessToken);

    // Tạo refresh token
    String newRefreshToken = securityUtil.createRefreshToken(email, loginResDTO);
    userService.updateUserWithRefreshToken(loginUser, newRefreshToken);

    return loginResDTO;
  }

  @Override
  public void recoverPassword(String email) throws EmailInvalidException {
    // check exist user with email
    User user = userRepository.findByEmailAndActivatedTrue(email)
        .orElseThrow(() -> new EmailInvalidException(ErrorMessage.EMAIL_INVALID));

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

  private LoginResDTO convertUserToResLoginDTO(User loginUser) {
    LoginResDTO.ResUser resUser = new LoginResDTO.ResUser();
    resUser.setId(loginUser.getId());
    resUser.setEmail(loginUser.getEmail());
    if (loginUser.getCustomer() != null) {
      resUser.setFirstName(loginUser.getCustomer().getFirstName());
      resUser.setLastName(loginUser.getCustomer().getLastName());
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
}