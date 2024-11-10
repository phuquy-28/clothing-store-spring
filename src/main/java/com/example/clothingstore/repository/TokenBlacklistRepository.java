package com.example.clothingstore.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.TokenBlacklist;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
  Optional<TokenBlacklist> findByToken(String token);

  void deleteByExpiryDateBefore(Instant now);
}
