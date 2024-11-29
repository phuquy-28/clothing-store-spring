package com.example.clothingstore.constant;

public final class AppConstant {

  public static final String ROLE_USER = "USER";
  public static final String ROLE_STAFF = "STAFF";
  public static final String ROLE_MANAGER = "MANAGER";
  public static final String ROLE_ADMIN = "ADMIN";

  // JWT
  public static final Long REFRESH_TOKEN_COOKIE_EXPIRE = 30L * 24 * 60 * 60;
  public static final Long COOKIE_INVALID_EXPIRE = 0L;

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  public static final String ACTIVATION_EMAIL_SUBJECT = "[ECOMMERCE FASHION] Activate your account";
  public static final String RECOVER_PASSWORD_EMAIL_SUBJECT = "[ECOMMERCE FASHION] Recover your password";
  public static final String ORDER_EMAIL_SUBJECT = "[ECOMMERCE FASHION] Order confirmation";
  public static final String ACTIVATION_EMAIL_TEMPLATE = "mail/activationEmail";
  public static final String RECOVER_PASSWORD_EMAIL_TEMPLATE = "mail/recoverPasswordEmail";
  public static final String ORDER_EMAIL_TEMPLATE = "mail/orderEmail";
  public static final String ORDER_CONFIRMATION_EMAIL_TEMPLATE = "mail/orderEmail";
  
  public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";

  private AppConstant() {
  }
}