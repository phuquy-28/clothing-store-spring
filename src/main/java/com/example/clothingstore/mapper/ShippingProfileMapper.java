package com.example.clothingstore.mapper;

import org.mapstruct.Mapper;
import com.example.clothingstore.dto.request.ShippingProfileReqDTO;
import com.example.clothingstore.dto.response.ShippingProfileResDTO;
import com.example.clothingstore.entity.ShippingProfile;

@Mapper(componentModel = "spring")
public interface ShippingProfileMapper {

  ShippingProfile toShippingProfile(ShippingProfileReqDTO shippingProfileReqDTO);

  ShippingProfileResDTO toShippingProfileResDTO(ShippingProfile shippingProfile);
}
