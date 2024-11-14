package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.ChangePasswordReqDTO;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.response.UserInfoDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.entity.Profile;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.entity.UserRefreshToken;
import com.example.clothingstore.enumeration.Gender;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final SecurityUtil securityUtil;

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
    profile.setGender(Gender.valueOf(editProfileReqDTO.getGender().toUpperCase()));
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
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    return UserInfoDTO.builder().email(user.getEmail() != null ? user.getEmail() : null)
        .firstName(
            user.getProfile().getFirstName() != null ? user.getProfile().getFirstName() : null)
        .lastName(user.getProfile().getLastName() != null ? user.getProfile().getLastName() : null)
        .role(user.getRole().getName() != null ? user.getRole().getName() : null).build();
  }
}
