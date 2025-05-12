package com.example.clothingstore.constant;

public final class AppConstant {

  // User Roles
  public static final String ROLE_USER = "USER";
  public static final String ROLE_STAFF = "STAFF";
  public static final String ROLE_MANAGER = "MANAGER";
  public static final String ROLE_ADMIN = "ADMIN";

  // Authentication & Security
  public static final Long REFRESH_TOKEN_COOKIE_EXPIRE = 30L * 24 * 60 * 60; // 30 days in seconds
  public static final Long COOKIE_INVALID_EXPIRE = 0L;
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

  // Email Subject Constants
  public static final String ACTIVATION_EMAIL_SUBJECT = "[EZSTORE] Activate your account";
  public static final String ACTIVATION_CODE_EMAIL_SUBJECT =
      "[EZSTORE] Activate your account";
  public static final String RESET_CODE_EMAIL_SUBJECT = "[EZSTORE] Recover your password";
  public static final String RECOVER_PASSWORD_EMAIL_SUBJECT =
      "[EZSTORE] Recover your password";
  public static final String ORDER_EMAIL_SUBJECT = "[EZSTORE] Order confirmation";
  public static final String PROFILE_OTP_MOBILE_EMAIL_SUBJECT =
      "[EZSTORE] OTP for profile";

  // Email Template Paths
  public static final String ACTIVATION_EMAIL_TEMPLATE = "mail/activationEmail";
  public static final String ACTIVATION_CODE_EMAIL_TEMPLATE = "mail/activationCodeEmail";
  public static final String RESET_CODE_EMAIL_TEMPLATE = "mail/recoverPasswordCodeEmail";
  public static final String RECOVER_PASSWORD_EMAIL_TEMPLATE = "mail/recoverPasswordEmail";
  public static final String ORDER_EMAIL_TEMPLATE = "mail/orderEmail";
  public static final String ORDER_CONFIRMATION_EMAIL_TEMPLATE = "mail/orderEmail";
  public static final String PROFILE_OTP_MOBILE_EMAIL_TEMPLATE = "mail/profileOtpMobileEmail";

  // Date Format
  public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";

  // Order Related
  public static final String ORDER_CANCEL_REASON = "Quá thời gian thanh toán";

  private AppConstant() {}
}
