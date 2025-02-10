package com.example.clothingstore.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.ChangePasswordReqDTO;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.request.UpdateProfileMobileReqDTO;
import com.example.clothingstore.dto.request.UpdateUserReqDTO;
import com.example.clothingstore.dto.request.UserReqDTO;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.dto.response.ProfileResMobileDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.RoleResDTO;
import com.example.clothingstore.dto.response.UserInfoDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.service.UserService;
import com.example.clothingstore.util.SecurityUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class UserController {

  private final Logger log = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  @GetMapping(UrlConfig.USER + UrlConfig.PROFILE)
  public ResponseEntity<ProfileResDTO> getProfile() {
    log.debug("REST request to get profile");
    return ResponseEntity.status(HttpStatus.OK).body(userService.getProfile());
  }

  @PutMapping(UrlConfig.USER + UrlConfig.EDIT_PROFILE)
  public ResponseEntity<UserResDTO> editProfile(
      @RequestBody @Valid EditProfileReqDTO editProfileReqDTO) {
    log.debug("REST request to edit profile: {}", editProfileReqDTO);
    return ResponseEntity.status(HttpStatus.OK).body(userService.editProfile(editProfileReqDTO));
  }

  @PutMapping(UrlConfig.USER + UrlConfig.CHANGE_PASSWORD)
  public ResponseEntity<Void> changePassword(
      @RequestBody @Valid ChangePasswordReqDTO changePasswordReqDTO) {
    log.debug("REST request to change password: {}", changePasswordReqDTO);
    userService.changePassword(changePasswordReqDTO);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping(UrlConfig.USER + UrlConfig.INFO)
  public ResponseEntity<UserInfoDTO> getUserInfo() {
    String email = SecurityUtil.getCurrentUserLogin().orElse(null);
    log.debug("REST request to get user info: {}", email);
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUserInfo(email));
  }

  @GetMapping(UrlConfig.USER + UrlConfig.ROLES)
  public ResponseEntity<List<RoleResDTO>> getRoles() {
    log.debug("REST request to get roles");
    return ResponseEntity.status(HttpStatus.OK).body(userService.getRoles());
  }

  @PostMapping(UrlConfig.USER)
  public ResponseEntity<UserResDTO> createUser(@RequestBody @Valid UserReqDTO userReqDTO) {
    log.debug("REST request to create user: {}", userReqDTO);
    return ResponseEntity.status(HttpStatus.OK).body(userService.createUser(userReqDTO));
  }

  @GetMapping(UrlConfig.USER)
  public ResponseEntity<ResultPaginationDTO> getUsers(@Filter Specification<User> spec,
      Pageable pageable) {
    log.debug("REST request to get users");
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUsers(spec, pageable));
  }

  @GetMapping(UrlConfig.USER + UrlConfig.ID)
  public ResponseEntity<UserResDTO> getUser(@PathVariable Long id) {
    log.debug("REST request to get user: {}", id);
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUser(id));
  }

  @PutMapping(UrlConfig.USER)
  public ResponseEntity<UserResDTO> updateUser(
      @RequestBody @Valid UpdateUserReqDTO updateUserReqDTO) {
    log.debug("REST request to update user: {}", updateUserReqDTO);
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(updateUserReqDTO));
  }

  @DeleteMapping(UrlConfig.USER + UrlConfig.ID)
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    log.debug("REST request to delete user: {}", id);
    userService.deleteUser(id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping(UrlConfig.MOBILE + UrlConfig.USER + UrlConfig.PROFILE)
  public ResponseEntity<ProfileResMobileDTO> getProfileMobile() {
    log.debug("REST request to get profile mobile");
    return ResponseEntity.status(HttpStatus.OK).body(userService.getProfileMobile());
  }

  @GetMapping(UrlConfig.MOBILE + UrlConfig.USER + UrlConfig.PROFILE + UrlConfig.SEND_OTP)
  public ResponseEntity<Void> sendOtpMobile(@RequestParam("email") String email) {
    log.debug("REST request to send otp mobile");
    userService.sendProfileOtpMobile(email);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping(UrlConfig.MOBILE + UrlConfig.USER + UrlConfig.PROFILE)
  public ResponseEntity<ProfileResMobileDTO> updateProfileMobile(
      @RequestBody @Valid UpdateProfileMobileReqDTO updateProfileMobileReqDTO) {
    log.debug("REST request to update profile mobile");
    return ResponseEntity.status(HttpStatus.OK)
        .body(userService.updateProfileMobile(updateProfileMobileReqDTO));
  }
}
