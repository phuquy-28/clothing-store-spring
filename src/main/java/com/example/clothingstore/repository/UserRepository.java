package com.example.clothingstore.repository;

import com.example.clothingstore.domain.User;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String username);

  Optional<User> findByActivationKey(String key);

  Optional<User> findByEmailAndActivatedFalse(String email);

  Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);
}
