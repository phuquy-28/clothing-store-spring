package com.example.clothingstore.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.SetDefaultProfileReqDTO;
import com.example.clothingstore.dto.request.ShippingProfileReqDTO;
import com.example.clothingstore.dto.response.ShippingProfileResDTO;
import com.example.clothingstore.service.ShippingProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class ShippingProfileController {

  private final Logger log = LoggerFactory.getLogger(ShippingProfileController.class);

  private final ShippingProfileService shippingProfileService;

  @PostMapping(UrlConfig.SHIPPING_PROFILE)
  public ResponseEntity<ShippingProfileResDTO> createShippingProfile(
      @RequestBody @Valid ShippingProfileReqDTO shippingProfileReqDTO) {
    log.debug("Create shipping profile request: {}", shippingProfileReqDTO);
    return ResponseEntity.ok(shippingProfileService.createShippingProfile(shippingProfileReqDTO));
  }

  @GetMapping(UrlConfig.SHIPPING_PROFILE)
  public ResponseEntity<List<ShippingProfileResDTO>> getShippingProfiles() {
    return ResponseEntity.ok(shippingProfileService.getShippingProfiles());
  }

  @GetMapping(UrlConfig.SHIPPING_PROFILE + UrlConfig.ID)
  public ResponseEntity<ShippingProfileResDTO> getShippingProfile(@PathVariable Long id) {
    return ResponseEntity.ok(shippingProfileService.getShippingProfile(id));
  }

  @PutMapping(UrlConfig.SHIPPING_PROFILE)
  public ResponseEntity<ShippingProfileResDTO> updateShippingProfile(
      @RequestBody @Valid ShippingProfileReqDTO shippingProfileReqDTO) {
    return ResponseEntity.ok(shippingProfileService.updateShippingProfile(shippingProfileReqDTO));
  }

  @DeleteMapping(UrlConfig.SHIPPING_PROFILE + UrlConfig.ID)
  public ResponseEntity<Void> deleteShippingProfile(@PathVariable Long id) {
    shippingProfileService.deleteShippingProfile(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.SHIPPING_PROFILE + UrlConfig.DEFAULT)
  public ResponseEntity<Void> setDefaultShippingProfile(
      @RequestBody @Valid SetDefaultProfileReqDTO setDefaultProfileReqDTO) {
    shippingProfileService
        .setDefaultShippingProfile(setDefaultProfileReqDTO.getShippingProfileId());
    return ResponseEntity.ok().build();
  }

  @GetMapping(UrlConfig.SHIPPING_PROFILE + UrlConfig.DEFAULT)
  public ResponseEntity<ShippingProfileResDTO> getDefaultShippingProfile() {
    return ResponseEntity.ok(shippingProfileService.getDefaultShippingProfile());
  }
}
