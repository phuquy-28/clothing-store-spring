package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.ChangePasswordReqDTO;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.request.UserReqDTO;
import com.example.clothingstore.dto.response.UserInfoDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.RoleResDTO;
import com.example.clothingstore.entity.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {

  User handleGetUserByUsername(String username);

  void updateUserWithRefreshToken(User user, String refreshToken);

  UserResDTO editProfile(EditProfileReqDTO editProfileReqDTO);

  void changePassword(ChangePasswordReqDTO changePasswordReqDTO);

  UserInfoDTO getUserInfo(String email);

  ProfileResDTO getProfile();

  List<RoleResDTO> getRoles();

  UserResDTO createUser(UserReqDTO userReqDTO);

  ResultPaginationDTO getUsers(Specification<User> spec, Pageable pageable);
}
