package com.example.clothingstore.util;

import java.security.SecureRandom;
import org.apache.commons.lang3.RandomStringUtils;

public final class RandomUtil {
  private static final int KEY_LENGTH = 50;
  private static final int CODE_LENGTH = 6;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private RandomUtil() {}

  public static String generateRandomAlphanumericString() {
    return RandomStringUtils.random(KEY_LENGTH, 0, 0, true, true, (char[]) null,
        SECURE_RANDOM);
  }

  public static String generateRandomNumericString() {
    return RandomStringUtils.random(CODE_LENGTH, 0, 0, false, true, (char[]) null,
        SECURE_RANDOM);
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

  public static String generateActivationCode() {
    return generateRandomNumericString();
  }

  public static String generateResetCode() {
    return generateRandomNumericString();
  }

  public static String generateProfileCode() {
    return generateRandomNumericString();
  }

  static {
    SECURE_RANDOM.nextBytes(new byte[64]);
  }
}
