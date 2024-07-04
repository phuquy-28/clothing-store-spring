package com.example.clothingstore.utils;

import java.security.SecureRandom;
import org.apache.commons.lang3.RandomStringUtils;

public final class RandomUtil {
  private static final int DEF_COUNT = 50;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private RandomUtil() {
  }

  public static String generateRandomAlphanumericString() {
    return RandomStringUtils.random(DEF_COUNT, 0, 0, true, true, (char[])null, SECURE_RANDOM);
  }

  public static String generatePassword() {
    return generateRandomAlphanumericString();
  }

  public static String generateActivationKey() {
    return generateRandomAlphanumericString();
  }

  public static String generateResetKey() {
    return generateRandomAlphanumericString();
  }

  static {
    SECURE_RANDOM.nextBytes(new byte[64]);
  }
}
