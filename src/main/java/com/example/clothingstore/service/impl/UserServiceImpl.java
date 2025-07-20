package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.AvatarReqDTO;
import com.example.clothingstore.dto.request.ChangePasswordReqDTO;
import com.example.clothingstore.dto.request.CreatePasswordReqDTO;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.request.UpdateProfileMobileReqDTO;
import com.example.clothingstore.dto.request.UpdateUserReqDTO;
import com.example.clothingstore.dto.request.UserReqDTO;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.dto.response.ProfileResMobileDTO;
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
import com.example.clothingstore.service.EmailService;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.RandomUtil;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final SecurityUtil securityUtil;

  private final CartService cartService;

  private final ProfileMapper profileMapper;

  private final RoleRepository roleRepository;

  private final UserMapper userMapper;

  private final EmailService emailService;

  public User handleGetUserByUsername(String username) {
    if (userRepository.findByEmail(username).isPresent()) {
      return userRepository.findByEmailAndActivatedTrue(username).get();
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
    profile.setFullName(String.format("%s %s", editProfileReqDTO.getLastName(), editProfileReqDTO.getFirstName()));
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
        .cartItemsCount(cartItemsCount)
        .avatar(user.getProfile().getAvatar() != null ? user.getProfile().getAvatar() : null)
        .hasPassword(checkUserHasPassword(user))
        .build();
  }

  @Override
  public ProfileResDTO getProfile() {
    User user = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    ProfileResDTO profileResDTO = profileMapper.toProfileResDTO(user.getProfile());
    profileResDTO.setEmail(user.getEmail());
    profileResDTO.setRole(
        RoleResDTO.builder().id(user.getRole().getId()).name(user.getRole().getName()).build());
    profileResDTO.setHasPassword(checkUserHasPassword(user));

    return profileResDTO;
  }

  @Override
  public boolean checkUserHasPassword(User user) {
    // Check if the user has a googleId (logged in via Google)
    // and if their password is null or empty
    if (user.getGoogleId() != null
        && (user.getPassword() == null || user.getPassword().isEmpty())) {
      return false;
    }
    return true;
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
    
    // Store raw password for email
    String rawPassword = userReqDTO.getPassword();
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setActivated(true);
    user.setRole(role);

    Profile profile = new Profile();
    profile.setFirstName(userReqDTO.getFirstName());
    profile.setLastName(userReqDTO.getLastName());
    profile.setFullName(String.format("%s %s", userReqDTO.getLastName(), userReqDTO.getFirstName()));
    profile.setGender(
        userReqDTO.getGender() != null ? Gender.valueOf(userReqDTO.getGender().toUpperCase())
            : null);
    profile.setBirthDate(userReqDTO.getBirthDate() != null ? userReqDTO.getBirthDate() : null);
    profile.setPhoneNumber(userReqDTO.getPhone() != null ? userReqDTO.getPhone() : null);
    profile.setUser(user);
    user.setProfile(profile);

    User savedUser = userRepository.save(user);

    // Send email notification with account details
    try {
      emailService.sendNewUserAccountEmail(savedUser, rawPassword);
    } catch (Exception e) {
      log.error("Failed to send account creation email to: ", e);
    }

    return userMapper.toUserResDTO(savedUser);
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
  public UserResDTO updateUser(UpdateUserReqDTO updateUserReqDTO) {
    Long userId = updateUserReqDTO.getId() != null ? updateUserReqDTO.getId() : null;

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    Role role = roleRepository.findById(updateUserReqDTO.getRoleId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ROLE_NOT_FOUND));

    if (updateUserReqDTO.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(updateUserReqDTO.getPassword()));
    }
    user.setRole(role);
    
    Profile profile = user.getProfile();
    profile.setFirstName(updateUserReqDTO.getFirstName());
    profile.setLastName(updateUserReqDTO.getLastName());
    profile.setFullName(String.format("%s %s", updateUserReqDTO.getLastName(), updateUserReqDTO.getFirstName()));
    profile.setGender(
        updateUserReqDTO.getGender() != null ? Gender.valueOf(updateUserReqDTO.getGender().toUpperCase())
            : null);
    profile.setBirthDate(updateUserReqDTO.getBirthDate() != null ? updateUserReqDTO.getBirthDate() : null);
    profile.setPhoneNumber(updateUserReqDTO.getPhone() != null ? updateUserReqDTO.getPhone() : null);
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

  @Override
  public Long countActivatedUsers() {
    return userRepository.countByActivatedTrue();
  }

  @Override
  public ProfileResMobileDTO getProfileMobile() {
    Profile profile = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND)).getProfile();
    return profileMapper.toProfileResMobileDTO(profile);
  }

  @Override
  public void sendProfileOtpMobile(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    Instant now = Instant.now();
    if (user.getProfileCodeDate() != null
        && Duration.between(user.getProfileCodeDate(), now).toSeconds() < 30) {
      throw new BadRequestException(ErrorMessage.OTP_NOT_SENT);
    }

    String otp = RandomUtil.generateProfileCode();
    user.setProfileCode(otp);
    user.setProfileCodeDate(now);
    userRepository.save(user);

    emailService.sendProfileOtpMobile(user);
  }

  @Override
  public ProfileResMobileDTO updateProfileMobile(
      UpdateProfileMobileReqDTO updateProfileMobileReqDTO) {
    User user = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    if (updateProfileMobileReqDTO.getOtp() == null || updateProfileMobileReqDTO.getOtp().isEmpty()) {
      Profile profile = user.getProfile();
      profile.setFirstName(updateProfileMobileReqDTO.getFirstName());
      profile.setLastName(updateProfileMobileReqDTO.getLastName());
      profile.setGender(Gender.valueOf(updateProfileMobileReqDTO.getGender().toUpperCase()));
      if (updateProfileMobileReqDTO.getBirthDate() != null)
        profile.setBirthDate(updateProfileMobileReqDTO.getBirthDate());
      userRepository.save(user);
    } else {
      if (!updateProfileMobileReqDTO.getOtp().equals(user.getProfileCode())) {
        throw new BadRequestException(ErrorMessage.OTP_INVALID);
      }

      Profile profile = user.getProfile();
      profile.setFirstName(updateProfileMobileReqDTO.getFirstName());
      profile.setLastName(updateProfileMobileReqDTO.getLastName());
      profile.setGender(Gender.valueOf(updateProfileMobileReqDTO.getGender().toUpperCase()));
      if (updateProfileMobileReqDTO.getBirthDate() != null)
        profile.setBirthDate(updateProfileMobileReqDTO.getBirthDate());
      profile.setPhoneNumber(updateProfileMobileReqDTO.getPhoneNumber());
      user.setEmail(updateProfileMobileReqDTO.getEmail());
      user.setProfileCode(null);
      user.setProfileCodeDate(null);
      userRepository.save(user);
    }

    return profileMapper.toProfileResMobileDTO(user.getProfile());
  }

  @Override
  public ProfileResMobileDTO updateAvatar(AvatarReqDTO avatarReqDTO) {
    User user = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().get())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));
        
    user.getProfile().setAvatar(avatarReqDTO.getAvatar());
    userRepository.save(user);

    return profileMapper.toProfileResMobileDTO(user.getProfile());
  }

  @Override
  public void createPassword(CreatePasswordReqDTO createPasswordReqDTO) {
    // Get current user from security context
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElse(null))
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    // Check if user already has a password
    if (checkUserHasPassword(currentUser)) {
      throw new BadRequestException(ErrorMessage.USER_ALREADY_HAS_PASSWORD);
    }

    // Validate password confirmation
    if (!createPasswordReqDTO.getPassword().equals(createPasswordReqDTO.getConfirmPassword())) {
      throw new BadRequestException(ErrorMessage.PASSWORD_NOT_MATCH);
    }

    // Encode and save password
    String encodedPassword = passwordEncoder.encode(createPasswordReqDTO.getPassword());
    currentUser.setPassword(encodedPassword);
    userRepository.save(currentUser);
  }
}
