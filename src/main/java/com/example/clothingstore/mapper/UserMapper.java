package com.example.clothingstore.mapper;

import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "profile.firstName", target = "firstName")
  @Mapping(source = "profile.lastName", target = "lastName")
  RegisterResDTO toRegisterResDTO(User user);

  @Mapping(source = "profile.firstName", target = "firstName")
  @Mapping(source = "profile.lastName", target = "lastName")
  @Mapping(source = "profile.birthDate", target = "birthDate")
  @Mapping(source = "profile.phoneNumber", target = "phoneNumber")
  @Mapping(source = "profile.gender", target = "gender")
  @Mapping(source = "role.id", target = "role.id")
  @Mapping(source = "role.name", target = "role.name")
  UserResDTO toUserResDTO(User user);
}
