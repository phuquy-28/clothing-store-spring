package com.example.clothingstore.scheduler;

import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.repository.TokenBlacklistRepository;
import com.example.clothingstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataCleanupScheduler {

  private final TokenBlacklistRepository tokenBlacklistRepository;

  private final UserRepository userRepository;

  @Scheduled(cron = "0 0 0 * * *") // Run daily
  @Transactional
  public void cleanupExpiredTokens() {
    log.info("Running expired token cleanup scheduler...");
    // Remove expired blacklisted tokens
    tokenBlacklistRepository.deleteByExpiryDateBefore(Instant.now());

    // Remove expired refresh tokens
    List<User> users = userRepository.findAll();
    for (User user : users) {
      boolean removed =
          user.getRefreshTokens().removeIf(rt -> rt.getExpiryDate().isBefore(Instant.now()));
      if (removed) {
        userRepository.save(user);
      }
    }
  }
}
