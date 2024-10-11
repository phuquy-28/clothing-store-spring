package com.example.clothingstore.service;

import java.util.List;
import com.example.clothingstore.dto.request.ShippingProfileReqDTO;
import com.example.clothingstore.dto.response.ShippingProfileResDTO;

public interface ShippingProfileService {

  ShippingProfileResDTO createShippingProfile(ShippingProfileReqDTO shippingProfileReqDTO);

  List<ShippingProfileResDTO> getShippingProfiles();

  ShippingProfileResDTO getShippingProfile(Long id);

  ShippingProfileResDTO updateShippingProfile(ShippingProfileReqDTO shippingProfileReqDTO);

  void deleteShippingProfile(Long id);

  void setDefaultShippingProfile(Long id);

  ShippingProfileResDTO getDefaultShippingProfile();

}
