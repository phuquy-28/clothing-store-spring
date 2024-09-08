package com.example.clothingstore.config;

import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.service.UserService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component("userDetailsService")
@RequiredArgsConstructor
public class UserDetailCustom implements UserDetailsService {

  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    com.example.clothingstore.entity.User user = this.userService.handleGetUserByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException(ErrorMessage.USERNAME_OR_PASSWORD_INVALID);
    }

    return new User(user.getEmail(), user.getPassword(), Collections.singletonList(
        new SimpleGrantedAuthority(
            user.getRole() != null ? user.getRole().getName() : AppConstant.ROLE_USER)));

  }

}
