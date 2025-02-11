package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.AvatarReqDTO;
import com.example.clothingstore.dto.request.ChangePasswordReqDTO;
import com.example.clothingstore.dto.request.EditProfileReqDTO;
import com.example.clothingstore.dto.request.UpdateProfileMobileReqDTO;
import com.example.clothingstore.dto.request.UpdateUserReqDTO;
import com.example.clothingstore.dto.request.UserReqDTO;
import com.example.clothingstore.dto.response.UserInfoDTO;
import com.example.clothingstore.dto.response.UserResDTO;
import com.example.clothingstore.dto.response.ProfileResDTO;
import com.example.clothingstore.dto.response.ProfileResMobileDTO;
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

  UserResDTO updateUser(UpdateUserReqDTO updateUserReqDTO);

  void deleteUser(Long id);

  UserResDTO getUser(Long id);

  Long countActivatedUsers();

  ProfileResMobileDTO getProfileMobile();

  void sendProfileOtpMobile(String email);

  ProfileResMobileDTO updateProfileMobile(UpdateProfileMobileReqDTO updateProfileMobileReqDTO);

  ProfileResMobileDTO updateAvatar(AvatarReqDTO avatarReqDTO);
}
