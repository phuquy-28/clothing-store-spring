package com.example.clothingstore.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlConfig {

  public static String API_VERSION;

  @Value("${api.version}")
  public void setApiVersion(String apiVersion) {
    API_VERSION = "/" + apiVersion;
  }

  // Health check
  public static final String ACTUATOR = "/actuator";
  public static final String HEALTH = "/health";

  // Common
  public static final String ID = "/{id}";
  public static final String MOBILE = "/mobile";
  public static final String SEND_OTP = "/send-otp";
  public static final String UPLOAD = "/upload";

  // Public endpoints
  public static final String ROOT = "/";
  public static final String API_DOCS = "/v3/api-docs/**";
  public static final String SWAGGER_UI = "/swagger-ui/**";
  public static final String SWAGGER_UI_HTML = "/swagger-ui.html";

  // Auth controller
  public static final String AUTH = "/auth";
  public static final String REGISTER = "/register";
  public static final String LOGIN = "/login";
  public static final String LOGOUT = "/logout";
  public static final String REFRESH = "/refresh";
  public static final String SEND_ACTIVATION_EMAIL = "/send-activation-email";
  public static final String SEND_ACTIVATION_CODE = "/send-activation-code";
  public static final String ACTIVATE = "/activate";
  public static final String ACTIVATE_CODE = "/activate-code";
  public static final String RECOVER_PASSWORD = "/recover-password";
  public static final String RECOVER_PASSWORD_CODE = "/recover-password-code";
  public static final String RESET_PASSWORD = "/reset-password";
  public static final String RESET_PASSWORD_CODE = "/reset-password-code";
  public static final String VERIFY_RESET_CODE = "/verify-reset-code";
  public static final String GOOGLE_AUTH = "/google";

  // Product controller
  public static final String PRODUCT = "/products";
  public static final String PRODUCT_ID = "/ids";
  public static final String UPLOAD_IMAGES = "/upload-images";
  public static final String PRODUCT_SLUG = "/{slug}";
  public static final String RECOMMENDATIONS = "/recommendations";
  public static final String FOR_YOU = "/for-you";
  public static final String LOG_VIEW = "/log-view";

  // Category controller
  public static final String CATEGORY = "/categories";

  // User controller
  public static final String USER = "/users";
  public static final String PROFILE = "/profiles";
  public static final String EDIT_PROFILE = "/edit-profile";
  public static final String CHANGE_PASSWORD = "/change-password";
  public static final String CREATE_PASSWORD = "/create-password";
  public static final String INFO = "/info";
  public static final String ROLES = "/roles";
  public static final String AVATAR = "/avatar";

  // Shipping profile controller
  public static final String SHIPPING_PROFILE = "/shipping-profiles";
  public static final String DEFAULT = "/default";

  // Order controller
  public static final String ORDERS = "/orders";
  public static final String PREVIEW = "/preview";
  public static final String CHECK_OUT = "/check-out";
  public static final String USER_ORDERS = "/user";
  public static final String LINE_ITEM = "/line-items";
  public static final String ORDER_ID = "/{orderId}";
  public static final String ORDER_CODE = "/{orderCode}";
  public static final String STATUS = "/status";
  public static final String STATUS_HISTORY = "/status-history";
  public static final String CONTINUE_PAYMENT = "/continue-payment";
  public static final String STATISTICS = "/statistics";
  public static final String CHART = "/chart";
  public static final String BAR = "/bar";
  public static final String LINE = "/line";
  public static final String CANCEL = "/cancel";
  public static final String CHECK_QUANTITY = "/check-quantity";

  // Review controller
  public static final String REVIEW = "/reviews";
  public static final String PUBLISH = "/publish";

  // Return request controller
  public static final String RETURN_REQUESTS = "/return-requests";
  public static final String PROCESS = "/process";
  public static final String ADMIN = "/admin";
  public static final String RETURN_REQUESTS_USER = "/user";
  public static final String CASHBACK = "/cashback";

  // Payment controller
  public static final String PAYMENT = "/payment";
  public static final String VNPAY_RETURN = "/vnpay_return";

  // Promotion controller
  public static final String PROMOTION = "/promotions";
  public static final String PROMOTION_IMAGES = "/images";

  // Cart controller
  public static final String CART = "/carts";
  public static final String ITEMS = "/items";

  // Point controller
  public static final String POINT = "/points";
  public static final String USER_POINT = "/user";
  public static final String CURRENT = "/current";

  // Dashboard controller
  public static final String WORKSPACE = "/workspace";
  public static final String DASHBOARD = "/dashboard";
  public static final String SUMMARY = "/summary";
  public static final String REVENUE_BY_MONTH = "/revenue-by-month";
  public static final String IMPORT_TEMPLATE = "/import/template/{templateType}";
  public static final String IMPORT_PRODUCTS = "/import/products";
  public static final String IMPORT_CATEGORIES = "/import/categories";

  // Notification controller
  public static final String NOTIFICATION = "/notifications";
  public static final String UNREAD_COUNT = "/unread-count";
  public static final String MARK_READ = "/mark-read";
  public static final String MARK_READ_ALL = "/mark-read-all";
  public static final String PROMOTION_NOTIFICATION = "/notifications/promotion";
  public static final String WS = "/ws/**";
  public static final String SAVE_FCM_TOKEN = "/save-fcm-token";
  public static final String DELETE_FCM_TOKEN = "/delete-fcm-token";

  // Inventory controller
  public static final String INVENTORY = "/inventory";
  public static final String IMPORT = "/import";
  public static final String EXPORT = "/export";
  public static final String HISTORY = "/history";

  // Full paths for public endpoints
  public static String[] PUBLIC_ENDPOINTS() {
    return new String[] {ROOT, API_DOCS, SWAGGER_UI, SWAGGER_UI_HTML};
  }

  public static String[] PUBLIC_WS_ENDPOINTS() {
    return new String[] {WS,};
  }

  // Full paths for public GET endpoints
  public static String[] PUBLIC_GET_ENDPOINTS() {
    return new String[] {ACTUATOR + HEALTH, API_VERSION + AUTH + ACTIVATE + "/**",
        API_VERSION + AUTH + SEND_ACTIVATION_EMAIL + "/**",
        API_VERSION + AUTH + SEND_ACTIVATION_CODE + "/**", API_VERSION + AUTH + REFRESH,
        API_VERSION + PRODUCT, API_VERSION + PRODUCT + PRODUCT_SLUG,
        API_VERSION + PRODUCT + PRODUCT_ID + ID, API_VERSION + PRODUCT + PRODUCT_SLUG + REVIEW,
        API_VERSION + CATEGORY, API_VERSION + USER, API_VERSION + USER + INFO,
        API_VERSION + PAYMENT, API_VERSION + PAYMENT + VNPAY_RETURN,
        API_VERSION + MOBILE + USER + PROFILE + SEND_OTP,
        API_VERSION + PRODUCT + RECOMMENDATIONS + FOR_YOU};
  }

  // Full paths for public POST endpoints
  public static String[] PUBLIC_POST_ENDPOINTS() {
    return new String[] {API_VERSION + AUTH + LOGIN, API_VERSION + AUTH + REGISTER,
        API_VERSION + AUTH + RECOVER_PASSWORD, API_VERSION + AUTH + RECOVER_PASSWORD_CODE,
        API_VERSION + AUTH + RESET_PASSWORD + "/**", API_VERSION + AUTH + RESET_PASSWORD_CODE,
        API_VERSION + AUTH + ACTIVATE_CODE + "/**", API_VERSION + AUTH + VERIFY_RESET_CODE,
        API_VERSION + AUTH + GOOGLE_AUTH, API_VERSION + ORDERS,
        // API_VERSION + ORDERS + CHECK_OUT,
        API_VERSION + WORKSPACE + LOGIN};
  }

  // Full paths for public PUT endpoints
  public static String[] PUBLIC_PUT_ENDPOINTS() {
    return new String[] {
        // Add any public PUT endpoints here if needed
    };
  }
}
