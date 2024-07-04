package com.example.clothingstore.service.impl;

import com.example.clothingstore.domain.Customer;
import com.example.clothingstore.domain.User;
import com.example.clothingstore.domain.dto.request.auth.ReqLoginDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.response.auth.ResLoginDTO;
import com.example.clothingstore.domain.dto.response.user.ResCreateUser;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.AuthService;
import com.example.clothingstore.service.EmailService;
import com.example.clothingstore.utils.RandomUtil;
import com.example.clothingstore.utils.SecurityUtil;
import com.example.clothingstore.utils.error.EmailInvalidException;
import com.example.clothingstore.utils.error.TokenInvalidException;
import com.example.clothingstore.utils.validate.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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

  public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
      RoleRepository roleRepository, AuthenticationManagerBuilder authenticationManagerBuilder,
      UserService userService, SecurityUtil securityUtil, EmailService emailService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.userService = userService;
    this.securityUtil = securityUtil;
    this.emailService = emailService;
  }

  @Override
  public ResCreateUser register(ReqRegisterDTO user) throws EmailInvalidException {
    // check email is already in use
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new EmailInvalidException("Email is already in use");
    }

    // validate email
    if (!EmailValidator.isValidEmail(user.getEmail())){
      throw new EmailInvalidException("Email is invalid");
    }

    User newUser = new User();
    // email and password are required
    newUser.setEmail(user.getEmail());
    newUser.setPassword(passwordEncoder.encode(user.getPassword()));

    // set default role for new user
    newUser.setRole(roleRepository.findByName("USER").orElse(null));

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

    return ResCreateUser.builder().id(savedUser.getId()).email(savedUser.getEmail())
        .customer(savedUser.getCustomer()).createdAt(savedUser.getCreatedAt())
        .createdBy(savedUser.getCreatedBy()).build();
  }

  @Override
  public ResLoginDTO login(ReqLoginDTO reqLoginDto) {
    // Nạp input gồm username/password vào Security
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        reqLoginDto.getEmail(), reqLoginDto.getPassword());

    // Thực hiện xác thực
    Authentication authentication = authenticationManagerBuilder.getObject()
        .authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Lấy thông tin user
    User loginUser = userService.handleGetUserByUsername(reqLoginDto.getEmail());
    ResLoginDTO.ResUser resUser = new ResLoginDTO.ResUser();
    resUser.setId(loginUser.getId());
    resUser.setEmail(loginUser.getEmail());
    resUser.setActivated(loginUser.isActivated());
    // role is optional
    ResLoginDTO.RoleUser roleUser = new ResLoginDTO.RoleUser();
    if (loginUser.getRole() != null) {
      roleUser.setId(loginUser.getRole().getId());
      roleUser.setName(loginUser.getRole().getName());
      resUser.setRole(roleUser);
    }

    // Tạo response
    ResLoginDTO resLoginDTO = new ResLoginDTO();
    resLoginDTO.setUser(resUser);

    if (loginUser.isActivated()) {
      // Tạo access token
      String accessToken = securityUtil.createAccessToken(authentication.getName(), resLoginDTO);
      resLoginDTO.setAccessToken(accessToken);

      // Tạo refresh token
      String refreshToken = securityUtil.createRefreshToken(reqLoginDto.getEmail(), resLoginDTO);

      userService.updateUserWithRefreshToken(loginUser, refreshToken);
    }

    log.debug("Login Information for User: {}", loginUser);

    return resLoginDTO;
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
  public void activateAccount(String key) {
    if (userRepository.findByActivationKey(key).isPresent()) {
      User user = userRepository.findByActivationKey(key).get();
      user.setActivated(true);
      user.setActivationKey(null);
      userRepository.save(user);
      log.debug("Activated Information for User: {}", user);
    } else {
      throw new BadCredentialsException("Activation key is invalid");
    }
  }

  @Override
  public void sendActivationEmail(String email) {
    if (userRepository.findByEmailAndActivatedFalse(email).isPresent()) {
      User user = userRepository.findByEmail(email).get();
      emailService.sendActivationEmail(user);
    } else {
      throw new BadCredentialsException("Email is invalid");
    }
  }

  @Override
  public ResLoginDTO refreshToken(String refreshToken) throws TokenInvalidException {
    Jwt jwt = securityUtil.jwtDecoder(refreshToken);
    String email = jwt.getSubject();

    // check exist user with email and refresh token
    User loginUser = userRepository.findByEmailAndRefreshToken(email, refreshToken)
        .orElseThrow(() -> new TokenInvalidException("Invalid refresh token"));

    ResLoginDTO.ResUser resUser = new ResLoginDTO.ResUser();
    resUser.setId(loginUser.getId());
    resUser.setEmail(loginUser.getEmail());
    resUser.setActivated(loginUser.isActivated());
    // role is optional
    ResLoginDTO.RoleUser roleUser = new ResLoginDTO.RoleUser();
    if (loginUser.getRole() != null) {
      roleUser.setId(loginUser.getRole().getId());
      roleUser.setName(loginUser.getRole().getName());
      resUser.setRole(roleUser);
    }

    // Tạo response
    ResLoginDTO resLoginDTO = new ResLoginDTO();
    resLoginDTO.setUser(resUser);

    // Tạo access token
    String accessToken = securityUtil.createAccessToken(email, resLoginDTO);
    resLoginDTO.setAccessToken(accessToken);

    // Tạo refresh token
    String newRefreshToken = securityUtil.createRefreshToken(email, resLoginDTO);
    userService.updateUserWithRefreshToken(loginUser, newRefreshToken);

    return resLoginDTO;
  }
}
