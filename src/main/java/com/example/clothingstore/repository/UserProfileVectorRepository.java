package com.example.clothingstore.repository;

import com.example.clothingstore.entity.UserProfileVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileVectorRepository extends JpaRepository<UserProfileVector, Long> {
  Optional<UserProfileVector> findByUserId(Long userId);
}
