package com.example.clothingstore.repository;

import com.example.clothingstore.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByActivationKey(String key);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.activationKey = :key")
  Optional<User> findByActivationKeyWithLock(@Param("key") String key);

  Optional<User> findByEmailAndActivatedFalse(String email);

  Optional<User> findByEmailAndActivatedTrue(String email);

  Optional<User> findByResetKey(String key);

  Page<User> findAll(Specification<User> spec, Pageable pageable);

  Long countByActivatedTrue();
}
