package com.example.clothingstore.config;

import com.example.clothingstore.service.UserService;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component("userDetailsService")
public class UserDetailCustom implements UserDetailsService {

  private final UserService userService;

  public UserDetailCustom(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    com.example.clothingstore.domain.User user = this.userService.handleGetUserByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("Username/password không hợp lệ");
    }
    return new User(user.getEmail(), user.getPassword(), Collections.singletonList(
        new SimpleGrantedAuthority(
            user.getRole() != null ? user.getRole().getName() : "ROLE_USER")));

  }

}
