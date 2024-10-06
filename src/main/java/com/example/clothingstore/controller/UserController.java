package com.example.clothingstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class UserController {

  private final Logger log = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  @PutMapping(UrlConfig.USER + UrlConfig.EDIT_PROFILE)
  public ResponseEntity<UserResDTO> editProfile(
      @RequestBody @Valid EditProfileReqDTO editProfileReqDTO) {
    log.debug("REST request to edit profile: {}", editProfileReqDTO);
    return ResponseEntity.status(HttpStatus.OK).body(userService.editProfile(editProfileReqDTO));
  }
}