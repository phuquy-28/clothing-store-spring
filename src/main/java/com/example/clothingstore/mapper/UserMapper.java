package com.example.clothingstore.mapper;

import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "profile.firstName", target = "firstName")
  @Mapping(source = "profile.lastName", target = "lastName")
  RegisterResDTO toRegisterResDTO(User user);
}
