package com.example.clothingstore.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.ShippingProfileReqDTO;
import com.example.clothingstore.dto.response.ShippingProfileResDTO;
import com.example.clothingstore.entity.ShippingProfile;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.exception.AccessDeniedException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.mapper.ShippingProfileMapper;
import com.example.clothingstore.repository.ShippingProfileRepository;
import com.example.clothingstore.repository.UserRepository;
import com.example.clothingstore.service.ShippingProfileService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShippingProfileServiceImpl implements ShippingProfileService {

  private final Logger log = LoggerFactory.getLogger(ShippingProfileServiceImpl.class);

  private final ShippingProfileMapper shippingProfileMapper;

  private final ShippingProfileRepository shippingProfileRepository;

  private final UserRepository userRepository;

  @Override
  public ShippingProfileResDTO createShippingProfile(ShippingProfileReqDTO shippingProfileReqDTO) {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND));

    ShippingProfile shippingProfile =
        shippingProfileMapper.toShippingProfile(shippingProfileReqDTO);
    shippingProfile.setUser(currentUser);

    ShippingProfile savedProfile = shippingProfileRepository.save(shippingProfile);
    log.debug("Saved shipping profile: {}", savedProfile);

    List<ShippingProfile> userProfiles = shippingProfileRepository.findByUser(currentUser);
    boolean isDefault = userProfiles.isEmpty();
    if (isDefault) {
      currentUser.setDefaultShippingProfile(savedProfile);
      userRepository.save(currentUser);
      log.debug("Set first shipping profile as default for user: {}", currentUser);
    }

    ShippingProfileResDTO shippingProfileResDTO =
        shippingProfileMapper.toShippingProfileResDTO(savedProfile);
    shippingProfileResDTO.setDefault(isDefault);
    return shippingProfileResDTO;
  }

  @Override
  public List<ShippingProfileResDTO> getShippingProfiles() {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND));
    List<ShippingProfile> shippingProfiles = shippingProfileRepository.findByUser(currentUser);
    return shippingProfiles.stream().map(shippingProfile -> {
      ShippingProfileResDTO shippingProfileResDTO =
          shippingProfileMapper.toShippingProfileResDTO(shippingProfile);
      shippingProfileResDTO
          .setDefault(shippingProfile.equals(currentUser.getDefaultShippingProfile()));
      return shippingProfileResDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public ShippingProfileResDTO getShippingProfile(Long id) {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND));

    ShippingProfile shippingProfile = shippingProfileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND));

    if (!shippingProfile.getUser().equals(currentUser)) {
      throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
    }

    ShippingProfileResDTO shippingProfileResDTO =
        shippingProfileMapper.toShippingProfileResDTO(shippingProfile);
    shippingProfileResDTO
        .setDefault(shippingProfile.equals(currentUser.getDefaultShippingProfile()));
    return shippingProfileResDTO;
  }

  @Override
  public ShippingProfileResDTO updateShippingProfile(ShippingProfileReqDTO shippingProfileReqDTO) {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND));

    ShippingProfile shippingProfile =
        shippingProfileRepository.findById(shippingProfileReqDTO.getId()).orElseThrow(
            () -> new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND));

    if (!shippingProfile.getUser().equals(currentUser)) {
      throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
    }

    shippingProfile.setFirstName(shippingProfileReqDTO.getFirstName());
    shippingProfile.setLastName(shippingProfileReqDTO.getLastName());
    shippingProfile.setPhoneNumber(shippingProfileReqDTO.getPhoneNumber());
    shippingProfile.setAddress(shippingProfileReqDTO.getAddress());
    shippingProfile.setWard(shippingProfileReqDTO.getWard());
    shippingProfile.setDistrict(shippingProfileReqDTO.getDistrict());
    shippingProfile.setProvince(shippingProfileReqDTO.getProvince());
    shippingProfile.setCountry(shippingProfileReqDTO.getCountry());

    ShippingProfile updatedProfile = shippingProfileRepository.save(shippingProfile);
    log.debug("Updated shipping profile: {}", updatedProfile);

    ShippingProfileResDTO shippingProfileResDTO =
        shippingProfileMapper.toShippingProfileResDTO(updatedProfile);
    shippingProfileResDTO
        .setDefault(updatedProfile.equals(currentUser.getDefaultShippingProfile()));
    return shippingProfileResDTO;
  }

  @Override
  @Transactional
  public void deleteShippingProfile(Long id) {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    ShippingProfile shippingProfile = shippingProfileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND));

    if (!shippingProfile.getUser().equals(currentUser)) {
      throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
    }

    // Check if this is the default shipping profile
    if (currentUser.getDefaultShippingProfile() != null
        && currentUser.getDefaultShippingProfile().getId().equals(shippingProfile.getId())) {
      currentUser.setDefaultShippingProfile(null);
      userRepository.save(currentUser);
    }

    shippingProfileRepository.delete(shippingProfile);
  }

  @Override
  public void setDefaultShippingProfile(Long id) {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    ShippingProfile shippingProfile = shippingProfileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND));

    if (!shippingProfile.getUser().equals(currentUser)) {
      throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
    }

    currentUser.setDefaultShippingProfile(shippingProfile);
    userRepository.save(currentUser);
  }

  @Override
  public ShippingProfileResDTO getDefaultShippingProfile() {
    User currentUser = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin().orElseThrow())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

    if (currentUser.getDefaultShippingProfile() == null) {
      throw new ResourceNotFoundException(ErrorMessage.SHIPPING_PROFILE_NOT_FOUND);
    }

    ShippingProfile defaultProfile = currentUser.getDefaultShippingProfile();
    ShippingProfileResDTO shippingProfileResDTO =
        shippingProfileMapper.toShippingProfileResDTO(defaultProfile);
    shippingProfileResDTO.setDefault(true);
    return shippingProfileResDTO;
  }
}
