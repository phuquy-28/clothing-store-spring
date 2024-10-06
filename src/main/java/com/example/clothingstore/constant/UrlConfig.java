package com.example.clothingstore.constant;

public interface UrlConfig {
  // Common
  String ID = "/{id}";

  // Auth controller
  String AUTH = "/auth";
  String REGISTER = "/register";
  String LOGIN = "/login";
  String LOGOUT = "/logout";
  String REFRESH = "/refresh";
  String SEND_ACTIVATION_EMAIL = "/send-activation-email";
  String ACTIVATE = "/activate";
  String RECOVER_PASSWORD = "/recover-password";
  String RESET_PASSWORD = "/reset-password";

  // Product controller
  String PRODUCT = "/products";
  String PRODUCT_SLUG = "/{slug}";

  // Category controller
  String CATEGORY = "/categories";

  // User controller
  String USER = "/users";
  String USER_ID = "/{id}";
  String PROFILE = "/profile";
  String EDIT_PROFILE = "/edit-profile";
}
