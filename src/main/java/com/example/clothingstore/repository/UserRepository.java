package com.example.clothingstore.repository;

import com.example.clothingstore.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByActivationKey(String key);

  Optional<User> findByEmailAndActivatedFalse(String email);

  Optional<User> findByEmailAndActivatedTrue(String email);

  Optional<User> findByResetKey(String key);

  Page<User> findAll(Specification<User> spec, Pageable pageable);
}
