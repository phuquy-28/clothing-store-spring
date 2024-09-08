package com.example.clothingstore.mapper;

import com.example.clothingstore.dto.response.RegisterResDTO;
import com.example.clothingstore.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "customer.firstName", target = "firstName")
  @Mapping(source = "customer.lastName", target = "lastName")
  RegisterResDTO toRegisterResDTO(User user);
}
