package com.example.clothingstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.dto.response.ProfileResMobileDTO;
import com.example.clothingstore.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

  ProfileResDTO toProfileResDTO(Profile profile);

  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.avatar", target = "avatar")
  ProfileResMobileDTO toProfileResMobileDTO(Profile profile);
}
