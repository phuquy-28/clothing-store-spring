package com.example.clothingstore.constant;

public interface AppConstant {

  String ROLE_USER = "USER";
  String ROLE_ADMIN = "ADMIN";

  // JWT
  Long REFRESH_TOKEN_COOKIE_EXPIRE = 30L * 24 * 60 * 60;
  Long COOKIE_INVALID_EXPIRE = 0L;

  String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
  String ACTIVATION_EMAIL_SUBJECT = "[ECOMMERCE FASHION] Activate your account";
  String RECOVER_PASSWORD_EMAIL_SUBJECT = "[ECOMMERCE FASHION] Recover your password";
  String ACTIVATION_EMAIL_TEMPLATE = "mail/activationEmail";
  String RECOVER_PASSWORD_EMAIL_TEMPLATE = "mail/recoverPasswordEmail";
}