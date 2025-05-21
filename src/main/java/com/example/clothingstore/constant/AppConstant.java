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
  public static final String ACTIVATION_CODE_EMAIL_SUBJECT = "[EZSTORE] Activate your account";
  public static final String RESET_CODE_EMAIL_SUBJECT = "[EZSTORE] Recover your password";
  public static final String RECOVER_PASSWORD_EMAIL_SUBJECT = "[EZSTORE] Recover your password";
  public static final String ORDER_EMAIL_SUBJECT = "[EZSTORE] Order confirmation";
  public static final String PROFILE_OTP_MOBILE_EMAIL_SUBJECT = "[EZSTORE] OTP for profile";
  public static final String NEW_USER_ACCOUNT_EMAIL_SUBJECT =
      "[EZSTORE] Your Account Has Been Created";

  // Email Template Paths
  public static final String ACTIVATION_EMAIL_TEMPLATE = "mail/activationEmail";
  public static final String ACTIVATION_CODE_EMAIL_TEMPLATE = "mail/activationCodeEmail";
  public static final String RESET_CODE_EMAIL_TEMPLATE = "mail/recoverPasswordCodeEmail";
  public static final String RECOVER_PASSWORD_EMAIL_TEMPLATE = "mail/recoverPasswordEmail";
  public static final String ORDER_EMAIL_TEMPLATE = "mail/orderEmail";
  public static final String ORDER_CONFIRMATION_EMAIL_TEMPLATE = "mail/orderEmail";
  public static final String PROFILE_OTP_MOBILE_EMAIL_TEMPLATE = "mail/profileOtpMobileEmail";
  public static final String NEW_USER_ACCOUNT_EMAIL_TEMPLATE = "mail/newUserAccountEmail";

  // Date Format
  public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";

  // Order Related
  public static final String ORDER_CANCEL_REASON = "Quá thời gian thanh toán";

  // Store Location Constants
  public static final double STORE_LATITUDE = 10.8514062;
  public static final double STORE_LONGITUDE = 106.755094;
  public static final String STORE_ADDRESS = "1 Đ. Võ Văn Ngân, Linh Chiểu, Thủ Đức, Hồ Chí Minh";

  // Express Delivery Fee Tiers (in VND)
  public static final double EXPRESS_FEE_TIER_1 = 30000; // 0-3 km
  public static final double EXPRESS_FEE_TIER_2 = 45000; // >3-6 km
  public static final double EXPRESS_FEE_TIER_3 = 60000; // >6-9 km
  public static final double EXPRESS_FEE_TIER_4 = 80000; // >9-12 km

  // Express Delivery Distance Tiers (in km)
  public static final double EXPRESS_DISTANCE_TIER_1_MIN = 0;
  public static final double EXPRESS_DISTANCE_TIER_1_MAX = 3;
  public static final double EXPRESS_DISTANCE_TIER_2_MIN = 3;
  public static final double EXPRESS_DISTANCE_TIER_2_MAX = 6;
  public static final double EXPRESS_DISTANCE_TIER_3_MIN = 6;
  public static final double EXPRESS_DISTANCE_TIER_3_MAX = 9;
  public static final double EXPRESS_DISTANCE_TIER_4_MIN = 9;
  public static final double EXPRESS_DISTANCE_TIER_4_MAX = 50;

  // HCMC Province ID
  public static final Long HCMC_PROVINCE_ID = 202L;

  // Delivery Area Restrictions
  public static final int MAX_DELIVERY_DISTANCE_KM = 50;
  public static final int MIN_DELIVERY_DISTANCE_KM = 0;

  private AppConstant() {}
}
