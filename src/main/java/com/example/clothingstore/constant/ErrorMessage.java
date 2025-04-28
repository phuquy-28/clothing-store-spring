package com.example.clothingstore.constant;

public final class ErrorMessage {

  // Authentication & Authorization Errors
  public static final String ACCESS_TOKEN_INVALID = "error.access_token.invalid";
  public static final String ACCESS_DENIED = "error.access_denied";
  public static final String REFRESH_TOKEN_INVALID = "error.refresh_token.invalid";
  public static final String USER_NOT_AUTHORIZED = "error.user.not_authorized";
  public static final String USER_NOT_LOGGED_IN = "error.user.not_logged_in";
  public static final String ROLE_NOT_FOUND = "error.role.not_found";

  // User Account Related Errors
  public static final String EMAIL_INVALID = "error.email.invalid";
  public static final String EMAIL_EXISTED = "error.email.existed";
  public static final String USER_NOT_FOUND = "error.user.not_found";
  public static final String USERNAME_OR_PASSWORD_INVALID = "error.username_or_password.invalid";
  public static final String OLD_PASSWORD_NOT_MATCH = "error.old_password.not_match";
  public static final String NEW_PASSWORD_NOT_MATCH = "error.new_password.not_match";
  public static final String PASSWORD_NOT_MATCH = "error.password.not_match";

  // Account Activation & Password Recovery
  public static final String ACTIVATION_TOKEN_INVALID = "error.activation_token.invalid";
  public static final String ACTIVATION_CODE_INVALID = "error.activation_code.invalid";
  public static final String RESET_TOKEN_INVALID = "error.reset_token.invalid";
  public static final String RESET_CODE_INVALID = "error.reset_code.invalid";
  public static final String RESET_CODE_EXPIRED = "error.reset_code.expired";
  public static final String PASSWORD_RECOVERY_TOO_FREQUENT =
      "error.password_recovery.too_frequent";

  // Product Related Errors
  public static final String PRODUCT_NOT_FOUND = "error.product.not_found";
  public static final String PRODUCT_ALREADY_EXISTS = "error.product.already_exists";
  public static final String PRODUCT_VARIANT_NOT_FOUND = "error.product_variant.not_found";
  public static final String NOT_ENOUGH_STOCK = "error.not_enough_stock";

  // Category Related Errors
  public static final String CATEGORY_NOT_FOUND = "error.category.not_found";
  public static final String CATEGORY_ALREADY_EXISTS = "error.category.already_exists";

  // Order & Cart Related Errors
  public static final String ORDER_NOT_FOUND = "error.order.not_found";
  public static final String ORDER_EXPIRED = "error.order.expired";
  public static final String ORDER_CANNOT_BE_CANCELLED = "error.order.cannot_be_cancelled";
  public static final String ORDER_STATUS_NOT_SUPPORTED = "error.order_status.not_supported";
  public static final String CART_NOT_FOUND = "error.cart.not_found";
  public static final String CART_ITEM_NOT_FOUND = "error.cart_item.not_found";
  public static final String LINE_ITEM_NOT_FOUND = "error.line_item.not_found";
  public static final String ORDER_CAN_ONLY_BE_RETURNED = "error.order.can_only_be_returned";
  public static final String ORDER_CAN_NOT_BE_RETURNED = "error.order.can_not_be_returned";

  // Payment Related Errors
  public static final String PAYMENT_FAILED = "error.payment.failed";
  public static final String PAYMENT_METHOD_NOT_SUPPORTED = "error.payment_method.not_supported";
  public static final String PAYMENT_STATUS_NOT_SUPPORTED = "error.payment_status.not_supported";
  public static final String PRE_PAYMENT_NOT_SUCCESS = "error.pre_payment.not_success";
  public static final String TRANSACTION_FAILED = "error.transaction.failed";
  public static final String INVALID_AMOUNT = "error.invalid.amount";
  public static final String INVALID_CHECKSUM = "error.invalid.checksum";

  // Shipping & Delivery Related Errors
  public static final String SHIPPING_PROFILE_NOT_FOUND = "error.shipping_profile.not_found";
  public static final String DEFAULT_SHIPPING_PROFILE_NOT_FOUND =
      "error.default_shipping_profile.not_found";
  public static final String DELIVERY_CALCULATION_FAILED = "error.delivery.calculation_failed";
  public static final String DELIVERY_AREA_NOT_SUPPORTED = "error.delivery.area_not_supported";
  public static final String DELIVERY_SERVICE_UNAVAILABLE = "error.delivery.service_unavailable";
  public static final String DELIVERY_FEE_CALCULATION_FAILED =
      "error.delivery.fee_calculation_failed";

  // Review Related Errors
  public static final String REVIEW_NOT_FOUND = "error.review.not_found";
  public static final String REVIEW_ALREADY_EXISTS = "error.review.already_exists";
  public static final String REVIEW_NOT_ALLOWED = "error.review.not_allowed";

  // Promotion Related Errors
  public static final String PROMOTION_NOT_FOUND = "error.promotion.not_found";
  public static final String PRODUCT_PROMOTION_NOT_FOUND = "error.product.not_found";
  public static final String CATEGORY_PROMOTION_NOT_FOUND = "error.category.not_found";

  // Point Related Errors
  public static final String POINT_ACTION_TYPE_INVALID = "error.point_action_type.invalid";
  public static final String POINT_NOT_ENOUGH = "error.point.not_enough";

  // Return request Errors
  public static final String RETURN_REQUEST_EXISTS = "error.return.request.already.exists";
  public static final String BANK_INFORMATION_REQUIRED = "error.bank.information.required";
  public static final String RETURN_REQUEST_NOT_FOUND = "error.return.request.not.found";

  // OTP Related Errors
  public static final String OTP_NOT_SENT = "error.otp.not_sent";
  public static final String OTP_INVALID = "error.otp.invalid";

  // General Errors
  public static final String ID_CANNOT_BE_NULL = "error.id.cannot_be_null";
  public static final String INVALID_FILE_TYPE = "error.file.invalid_type";
  public static final String SYSTEM_BUSY = "error.system.busy";

  // Notification Related Errors
  public static final String NOTIFICATION_NOT_FOUND = "error.notification.not_found";

  private ErrorMessage() {}
}
