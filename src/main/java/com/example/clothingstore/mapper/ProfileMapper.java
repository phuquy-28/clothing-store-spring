package com.example.clothingstore.mapper;

import org.mapstruct.Mapper;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

  ProfileResDTO toProfileResDTO(Profile profile);
}
