package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.ChangePasswordReqDTO;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.request.UserReqDTO;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO.Meta;
import com.example.clothingstore.dto.response.RoleResDTO;
import com.example.clothingstore.dto.response.UserInfoDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.entity.Profile;
import com.example.clothingstore.entity.Role;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.entity.UserRefreshToken;
import com.example.clothingstore.enumeration.Gender;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.mapper.ProfileMapper;
import com.example.clothingstore.mapper.UserMapper;
import com.example.clothingstore.repository.RoleRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.CartService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final SecurityUtil securityUtil;

  private final CartService cartService;

  private final ProfileMapper profileMapper;

  private final RoleRepository roleRepository;

  private final UserMapper userMapper;

  public User handleGetUserByUsername(String username) {
    if (userRepository.findByEmail(username).isPresent()) {
      return userRepository.findByEmail(username).get();
    }
    return null;
  }

  @Override
  public void updateUserWithRefreshToken(User user, String refreshToken) {
    // Decode refresh token to get expiry
    Jwt jwt = securityUtil.jwtDecoder(refreshToken);

    // Create new refresh token entity
    UserRefreshToken userRefreshToken = new UserRefreshToken();
    userRefreshToken.setRefreshToken(refreshToken);
    userRefreshToken.setCreatedDate(jwt.getIssuedAt());
    userRefreshToken.setExpiryDate(jwt.getExpiresAt());
    userRefreshToken.setUser(user);

    // Add to user's refresh tokens
    if (user.getRefreshTokens() == null) {
      user.setRefreshTokens(new ArrayList<>());
    }
    user.getRefreshTokens().add(userRefreshToken);

    userRepository.save(user);
  }

  @Override
  public UserResDTO editProfile(EditProfileReqDTO editProfileReqDTO) {
    User user = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    // Update user information
    Profile profile = user.getProfile();
    profile.setFirstName(editProfileReqDTO.getFirstName());
    profile.setLastName(editProfileReqDTO.getLastName());
    profile.setBirthDate(editProfileReqDTO.getBirthDate());
    profile.setPhoneNumber(editProfileReqDTO.getPhoneNumber());
    profile.setGender(editProfileReqDTO.getGender() != null
        ? Gender.valueOf(editProfileReqDTO.getGender().toUpperCase())
        : null);
    userRepository.save(user);

    return UserResDTO.builder().id(user.getId()).firstName(profile.getFirstName())
        .lastName(profile.getLastName()).birthDate(profile.getBirthDate().toString())
        .phoneNumber(profile.getPhoneNumber()).gender(profile.getGender()).email(user.getEmail())
        .build();
  }

  @Override
  public void changePassword(ChangePasswordReqDTO changePasswordReqDTO) {
    User user = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    if (!passwordEncoder.matches(changePasswordReqDTO.getOldPassword(), user.getPassword())) {
      throw new BadRequestException(ErrorMessage.OLD_PASSWORD_NOT_MATCH);
    }

    if (!changePasswordReqDTO.getNewPassword().equals(changePasswordReqDTO.getConfirmPassword())) {
      throw new BadRequestException(ErrorMessage.NEW_PASSWORD_NOT_MATCH);
    }

    user.setPassword(passwordEncoder.encode(changePasswordReqDTO.getNewPassword()));
    userRepository.save(user);
  }

  @Override
  public UserInfoDTO getUserInfo(String email) {
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      return null;
    }
    Long cartItemsCount = cartService.getCartItemsCount();

    return UserInfoDTO.builder().email(user.getEmail() != null ? user.getEmail() : null)
        .firstName(
            user.getProfile().getFirstName() != null ? user.getProfile().getFirstName() : null)
        .lastName(user.getProfile().getLastName() != null ? user.getProfile().getLastName() : null)
        .role(user.getRole().getName() != null ? user.getRole().getName() : null)
        .cartItemsCount(cartItemsCount).build();
  }

  @Override
  public ProfileResDTO getProfile() {
    Profile profile = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND)).getProfile();
    return profileMapper.toProfileResDTO(profile);
  }

  @Override
  public List<RoleResDTO> getRoles() {
    return roleRepository.findAll().stream().filter(Role::isActive)
        .map(role -> RoleResDTO.builder().id(role.getId()).name(role.getName()).build())
        .collect(Collectors.toList());
  }

  @Override
  public UserResDTO createUser(UserReqDTO userReqDTO) {
    if (userRepository.findByEmail(userReqDTO.getEmail()).isPresent()) {
      throw new BadRequestException(ErrorMessage.EMAIL_EXISTED);
    }

    Role role = roleRepository.findById(userReqDTO.getRoleId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ROLE_NOT_FOUND));

    User user = new User();
    user.setEmail(userReqDTO.getEmail());
    user.setPassword(passwordEncoder.encode(userReqDTO.getPassword()));
    user.setActivated(true);
    user.setRole(role);

    Profile profile = new Profile();
    profile.setFirstName(userReqDTO.getFirstName());
    profile.setLastName(userReqDTO.getLastName());
    profile.setGender(
        userReqDTO.getGender() != null ? Gender.valueOf(userReqDTO.getGender().toUpperCase())
            : null);
    profile.setBirthDate(userReqDTO.getBirthDate() != null ? userReqDTO.getBirthDate() : null);
    profile.setPhoneNumber(userReqDTO.getPhone() != null ? userReqDTO.getPhone() : null);
    profile.setUser(user);
    user.setProfile(profile);

    userRepository.save(user);

    return userMapper.toUserResDTO(user);
  }

  @Override
  public ResultPaginationDTO getUsers(Specification<User> spec, Pageable pageable) {
    Page<User> users = userRepository.findAll(spec, pageable);

    List<UserResDTO> userDTOs = users.getContent().stream().map(userMapper::toUserResDTO)
        .collect(Collectors.toList());
        
    return ResultPaginationDTO.builder().data(userDTOs)
        .meta(Meta.builder().page(Long.valueOf(pageable.getPageNumber()))
            .pageSize(Long.valueOf(pageable.getPageSize()))
            .pages(Long.valueOf(users.getTotalPages()))
            .total(Long.valueOf(users.getTotalElements())).build())
        .build();
  }

  @Override
  public UserResDTO updateUser(UserReqDTO userReqDTO) {
    Long userId = userReqDTO.getId() != null ? userReqDTO.getId() : null;

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    Role role = roleRepository.findById(userReqDTO.getRoleId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ROLE_NOT_FOUND));

    user.setPassword(passwordEncoder.encode(userReqDTO.getPassword()));
    user.setRole(role);
    
    Profile profile = user.getProfile();
    profile.setFirstName(userReqDTO.getFirstName());
    profile.setLastName(userReqDTO.getLastName());
    profile.setGender(
        userReqDTO.getGender() != null ? Gender.valueOf(userReqDTO.getGender().toUpperCase())
            : null);
    profile.setBirthDate(userReqDTO.getBirthDate() != null ? userReqDTO.getBirthDate() : null);
    profile.setPhoneNumber(userReqDTO.getPhone() != null ? userReqDTO.getPhone() : null);
    user.setProfile(profile);

    userRepository.save(user);

    return userMapper.toUserResDTO(user);
  }

  @Override
  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    userRepository.delete(user);
  }

  @Override
  public UserResDTO getUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    return userMapper.toUserResDTO(user);
  }
}
