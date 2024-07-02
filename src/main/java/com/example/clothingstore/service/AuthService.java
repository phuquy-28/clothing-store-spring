package com.example.clothingstore.service;

import com.example.clothingstore.domain.Customer;
import com.example.clothingstore.domain.User;
import com.example.clothingstore.domain.dto.request.auth.ReqRegisterDTO;
import com.example.clothingstore.domain.dto.response.user.ResCreateUser;
import com.example.clothingstore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public ResCreateUser register(ReqRegisterDTO user) {
    User newUser = new User();
    // email and password are required
    newUser.setEmail(user.getEmail());
    newUser.setPassword(passwordEncoder.encode(user.getPassword()));

    // firstName and lastName are optional
    Customer customer = new Customer();
    if (user.getFirstName() != null) {
      customer.setFirstName(user.getFirstName());
    }
    if (user.getLastName() != null) {
      customer.setLastName(user.getLastName());
    }

    newUser.setCustomer(customer);

    User savedUser = userRepository.save(newUser);

    return ResCreateUser.builder()
        .id(savedUser.getId())
        .email(savedUser.getEmail())
        .customer(savedUser.getCustomer())
        .createdAt(savedUser.getCreatedAt())
        .createdBy(savedUser.getCreatedBy())
        .build();
  }
}
