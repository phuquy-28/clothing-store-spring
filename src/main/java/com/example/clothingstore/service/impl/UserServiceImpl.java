package com.example.clothingstore.service.impl;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.entity.Profile;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.Gender;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public User handleGetUserByUsername(String username) {
    if (userRepository.findByEmail(username).isPresent()) {
      return userRepository.findByEmail(username).get();
    }
    return null;
  }

  public void updateUserWithRefreshToken(User user, String refreshToken) {
    user.setRefreshToken(refreshToken);
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
}
