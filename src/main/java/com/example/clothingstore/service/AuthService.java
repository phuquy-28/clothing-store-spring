package com.example.clothingstore.service;

import com.example.clothingstore.domain.Customer;
import com.example.clothingstore.domain.User;
import com.example.clothingstore.domain.dto.request.auth.ReqLoginDTO;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.response.auth.ResLoginDTO;
import com.example.clothingstore.domain.dto.response.user.ResCreateUser;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.utils.SecurityUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final RoleRepository roleRepository;

  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      RoleRepository roleRepository, AuthenticationManagerBuilder authenticationManagerBuilder,
      UserService userService, SecurityUtil securityUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.userService = userService;
    this.securityUtil = securityUtil;
  }

  public ResCreateUser register(ReqRegisterDTO user) {
    User newUser = new User();
    // email and password are required
    newUser.setEmail(user.getEmail());
    newUser.setPassword(passwordEncoder.encode(user.getPassword()));

    // set default role for new user
    newUser.setRole(roleRepository.findByName("USER").orElse(null));

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

    return ResCreateUser.builder().id(savedUser.getId()).email(savedUser.getEmail())
        .customer(savedUser.getCustomer()).createdAt(savedUser.getCreatedAt())
        .createdBy(savedUser.getCreatedBy()).build();
  }

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
    String accessToken = securityUtil.createAccessToken(authentication.getName(), resLoginDTO);
    resLoginDTO.setAccessToken(accessToken);

    // Tạo refresh token
    String refreshToken = securityUtil.createRefreshToken(reqLoginDto.getEmail(), resLoginDTO);

    userService.updateUserWithRefreshToken(loginUser, refreshToken);

    return resLoginDTO;
  }
}
